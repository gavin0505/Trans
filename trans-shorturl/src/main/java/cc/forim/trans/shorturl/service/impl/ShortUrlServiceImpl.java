package cc.forim.trans.shorturl.service.impl;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.common.enums.CommonConstant;
import cc.forim.trans.common.lock.DistributeLockFactory;
import cc.forim.trans.common.lock.DistributedLock;
import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.shorturl.cache.UrlMapCacheManager;
import cc.forim.trans.shorturl.dao.CompressionCodeMapper;
import cc.forim.trans.shorturl.dao.DomainConfMapper;
import cc.forim.trans.shorturl.dao.UrlMapMapper;
import cc.forim.trans.shorturl.infra.constant.*;
import cc.forim.trans.shorturl.infra.dto.*;
import cc.forim.trans.shorturl.infra.entity.CompressionCode;
import cc.forim.trans.shorturl.infra.entity.DomainConf;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import cc.forim.trans.shorturl.infra.exceptions.CreateCompressionCodeException;
import cc.forim.trans.shorturl.infra.exceptions.CreateShortUrlException;
import cc.forim.trans.shorturl.infra.exceptions.InsertException;
import cc.forim.trans.shorturl.infra.exceptions.SelectException;
import cc.forim.trans.shorturl.infra.register.SequenceGenerator;
import cc.forim.trans.shorturl.infra.utils.ConvertSequenceUtil;
import cc.forim.trans.shorturl.infra.vo.*;
import cc.forim.trans.shorturl.service.ShortUrlService;
import cc.forim.trans.shorturl.service.TransactionalService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cc.forim.trans.common.enums.CommonConstant.PROTOCOL_SP;
import static cc.forim.trans.shorturl.infra.constant.CacheKey.*;
import static cc.forim.trans.shorturl.infra.constant.CommonConstant.*;
import static cc.forim.trans.shorturl.infra.constant.CompressionCodeStatus.AVAILABLE;
import static cc.forim.trans.shorturl.infra.constant.CompressionCodeStatus.USED;
import static cc.forim.trans.shorturl.infra.constant.ExceptionEnum.*;

/**
 * 短链接操作服务实现类
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Service("shortUrlServiceImpl")
@Slf4j
public class ShortUrlServiceImpl implements ShortUrlService {

    @Resource(name = "distributeLockFactory")
    private DistributeLockFactory distributeLockFactory;

    @Resource(name = "compressionCodeMapper")
    private CompressionCodeMapper compressionCodeMapper;

    @Resource(name = "urlMapMapper")
    private UrlMapMapper urlMapMapper;

    @Resource(name = "domainConfMapper")
    private DomainConfMapper domainConfMapper;

    @Resource(name = "transactionalServiceImpl")
    private TransactionalService transactionalService;

    @Resource(name = "urlMapCacheManager")
    private UrlMapCacheManager urlMapCacheManager;

    @Resource(name = "snowflakeSequenceGenerator")
    private SequenceGenerator sequenceGenerator;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    private final UrlValidator urlValidator = new UrlValidator(new String[]{CommonConstant.HTTP_PROTOCOL,
            CommonConstant.HTTPS_PROTOCOL});

    /**
     * 低水位，触发生成短链
     */
    private static final int LWL = 100;

    /**
     * 单次生成压缩码数量
     */
    @Value("${trans.shortUrl.compress.code.batch}")
    private Integer compressCodeBatch;

    @Override
    public ResultVO<ShortUrlCreationVO> createShortUrlBiz(GenerateShortUrlDTO dto) {
        log.info("Receive the request of creating short_url: [requestId={}, longUrl={}]",
                dto.getRequestId(), dto.getLongUrl());
        // 1. 创建短链
        String shortUrl = createShortUrl(dto);
        // 2. 返回结果
        if (StrUtil.isNotBlank(shortUrl)) {
            // 设置VO
            ShortUrlCreationVO vo = new ShortUrlCreationVO();
            vo.setShortUrl(shortUrl);
            vo.setRequestId(dto.getRequestId());
            log.info("Get the short_url SUCCESS: [{}]", vo);
            return ResultVO.success(ResultStatusEnum.CREATE_SHORT_URL_SUCCESS.getCode(),
                    ResultStatusEnum.CREATE_SHORT_URL_SUCCESS.getDescription(), vo);
        }
        log.warn("Can't get the short_url: [requestId={}]", dto.getRequestId());
        return ResultVO.error(ResultStatusEnum.CREATE_SHORT_URL_FAILED.getCode(),
                ResultStatusEnum.CREATE_SHORT_URL_FAILED.getDescription());
    }

    @Override
    public ResultVO<ShortUrlRenewalVO> renewalShortUrlBiz(RenewalShortUrlDTO dto) {
        log.info("Receive the request of renew short_url: [requestId={}, shortUrl={}, longUrl={}]",
                dto.getRequestId(), dto.getShortUrl(), dto.getLongUrl());
        // 创建分布式锁
        DistributedLock lock = distributeLockFactory.provideDistributedLock(
                LockKey.EDIT_URL_MAP.getCode() + StrUtil.COLON
                        + dto.getDomain() + StrUtil.COLON
                        + dto.getBizType() + StrUtil.COLON
                        + dto.getCompressionCode()
        );
        try {
            // 上锁
            if (lock.tryLock(LockKey.EDIT_URL_MAP.getWaitTime(), LockKey.EDIT_URL_MAP.getReleaseTime(), TimeUnit.MILLISECONDS)) {

                ShortUrlRenewalVO vo = new ShortUrlRenewalVO();
                // 正在工作的短链
                List<UrlMap> workingUrlMaps = urlMapMapper.selectByUrlStatusAndShortUrl(UrlMapStatus.AVAILABLE.getValue(),
                        dto.getShortUrl()
                );
                // 待续期的短链
                List<UrlMap> targetUrlMaps = urlMapMapper.selectByLongUrlAndShortUrlAndUserId(
                        dto.getShortUrl(),
                        dto.getLongUrl(),
                        dto.getUserId()
                );
                // 续签时间一定要比晚于当前时间
                if (DateUtil.compare(dto.getInvalidateDate(), DateUtil.date()) > 0) {
                    // 续期的只能是：
                    // 1. 该短链处于失效时，曾经拥有过该短链的任何人；
                    if (ObjectUtil.isEmpty(workingUrlMaps)) {
                        if (ObjectUtil.isNotEmpty(targetUrlMaps)) {
                            UrlMap targetUrlMap = targetUrlMaps.get(0);
                            // 直接续
                            executeRenewalUrlMap(vo, dto, targetUrlMap);
                        } else {
                            return ResultVO.error("续签失败");
                        }
                    } else if (ObjectUtil.length(workingUrlMaps) == UrlMapStatus.AVAILABLE.getValue()) {
                        UrlMap workingUrlMap = workingUrlMaps.get(0);
                        // 2. 该短链处于生效时，拥有该短链映射的人(前提保证：任何正在运转的短链只有一条映射)
                        if (ObjectUtil.equals(workingUrlMap.getUserId(), dto.getUserId())) {
                            // 找到那条待续签的短链（因为该短链是由这个人控制，但未必是它想续期的短链，有可能他想续期该短链之前的若干个版本）
                            // 判断是否是正在工作的这个
                            UrlMap targetUrlMap = targetUrlMaps.get(0);
                            if (ObjectUtil.equals(workingUrlMap.getId(), targetUrlMap.getId())) {
                                // 是的话，直接续当前即可
                                executeRenewalUrlMap(vo, dto, targetUrlMap);
                            } else {
                                Map<String, String> shortUrlHashMap = disassembleShortUrl(workingUrlMap.getShortUrl());
                                // 否则先下线工作短链，再续目标短链

                                ShortUrlDeleteDTO deleteDTO = ShortUrlDeleteDTO.builder()
                                        .requestId(dto.getRequestId())
                                        .shortUrl(workingUrlMap.getShortUrl())
                                        .domain(shortUrlHashMap.get("domain"))
                                        .protocol(shortUrlHashMap.get("protocol"))
                                        .bizType(workingUrlMap.getBizType())
                                        .compressionCode(workingUrlMap.getCompressionCode())
                                        .userId(dto.getUserId())
                                        .id(workingUrlMap.getId())
                                        .build();
                                if (ObjectUtil.equals(deleteShortUrlBiz(deleteDTO).getCode(), SUCCESS)) {
                                    executeRenewalUrlMap(vo, dto, targetUrlMap);
                                } else {
                                    return ResultVO.error("续签失败");
                                }
                            }
                        } else {
                            return ResultVO.error("续签失败");
                        }
                    }
                    log.info("Renew the short_url of expire time SUCCESS: [{}]", dto);
                    return ResultVO.success(vo);
                } else {
                    return ResultVO.error("续签后时间不能早于原失效时间");
                }
            }
            return ResultVO.error("续签失败");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行续签操作（存储层面）
     *
     * @param vo     续签短链接视图
     * @param dto    续签短网址的传输数据
     * @param urlMap 最终续签的UrlMap
     */
    private void executeRenewalUrlMap(ShortUrlRenewalVO vo, RenewalShortUrlDTO dto, UrlMap urlMap) {
        urlMap.setExpireTime(dto.getInvalidateDate());
        urlMap.setUrlStatus(UrlMapStatus.AVAILABLE.getValue());
        // 改数据库
        transactionalService.saveShortUrlRenewalMessage(urlMap);
        // 改缓存
        urlMapCacheManager.refreshUrlMapCache(urlMap);

        vo.setRenewalTime(dto.getInvalidateDate());
        vo.setRequestId(dto.getRequestId());
        vo.setId(urlMap.getId());
        vo.setShortUrl(urlMap.getShortUrl());
    }

    @Override
    public ResultVO<ShortUrlQueryVO> getShortUrlBiz(ShortUrlQueryDTO dto) {

        ShortUrlQueryVO vo;
        UrlMap urlMap;
        QueryWrapper<UrlMap> wrapper = new QueryWrapper<>();
        assembleQueryShortUrlWrapper(wrapper, dto);
        urlMap = urlMapMapper.selectOne(wrapper);
        vo = urlMap2ShortUrlQueryVO(urlMap);

        return ResultVO.success(vo);
    }

    /**
     * UrlMap转ShortUrlQueryVO
     *
     * @param urlmap urlMap
     * @return ShortUrlQueryVO
     */
    private ShortUrlQueryVO urlMap2ShortUrlQueryVO(UrlMap urlmap) {
        ShortUrlQueryVO vo = new ShortUrlQueryVO();
        vo.setId(urlmap.getId());
        vo.setDescription(urlmap.getDescription());
        vo.setShortUrl(urlmap.getShortUrl());
        vo.setLongUrl(urlmap.getLongUrl());
        vo.setBizType(urlmap.getBizType());
        vo.setCreateTime(urlmap.getCreateTime());
        switch (urlmap.getUrlStatus()) {
            case 1:
                vo.setUrlStatus(UrlMapStatus.AVAILABLE.getStatus());
                break;
            case 2:
                vo.setUrlStatus(UrlMapStatus.INVALID.getStatus());
            default:
        }
        vo.setExpireTime(urlmap.getExpireTime());
        vo.setCompressionCode(urlmap.getCompressionCode());
        return vo;
    }

    /**
     * 拼装查询UrlMap的Wrapper
     *
     * @param wrapper wrapper
     * @param dto     短链接查询的传输数据
     */
    private void assembleQueryShortUrlWrapper(QueryWrapper<UrlMap> wrapper, ShortUrlQueryDTO dto) {
        if (ObjectUtil.isNotEmpty(dto.getId())) {
            wrapper.eq("id", dto.getId());
        }
        if (StrUtil.isNotEmpty(dto.getDomain()) &&
                StrUtil.isNotEmpty(dto.getProtocol()) &&
                StrUtil.isNotEmpty(dto.getBizType())) {
            DomainConf domainConf = domainConfMapper.selectIdByDomainAndProtocol(dto.getDomain(),
                    dto.getProtocol(),
                    dto.getBizType());
            if (ObjectUtil.isNotEmpty(domainConf.getId())) {
                wrapper.eq("domain_conf_id", domainConf.getId());
            }
        }
        if (ObjectUtil.isNotEmpty(dto.getShortUrl())) {
            wrapper.eq("short_url", dto.getShortUrl());
        }
        if (ObjectUtil.isNotEmpty(dto.getLongUrl())) {
            wrapper.eq("long_url", dto.getLongUrl());
        }
        if (ObjectUtil.isNotEmpty(dto.getCompressionCode())) {
            wrapper.eq("compression_code", dto.getCompressionCode());
        }
        if (ObjectUtil.isNotEmpty(dto.getExpireTime())) {
            wrapper.lt("expire_time", dto.getExpireTime());
        }
        if (ObjectUtil.isNotEmpty(dto.getUrlStatus())) {
            wrapper.eq("url_status", dto.getUrlStatus());
        }
        if (ObjectUtil.isNotEmpty(dto.getUserId())) {
            wrapper.eq("user_id", dto.getUserId());
        }

        if (ObjectUtil.isNotEmpty(dto.getDescription())) {
            wrapper.like("description", PERCENT_SIGN + dto.getDescription() + PERCENT_SIGN);
        }
    }

    @Override
    public ResultVO<ShortUrlQueryVO> updateShortUrlBiz(ShortUrlEditDTO dto) {
        log.info("Receive the request of update short_url: [requestId={}, url_map_id={}, long_url: {}, desc: {}]",
                dto.getRequestId(), dto.getId(), dto.getLongUrl(), dto.getDescription());
        // 先找到相关信息吧
        UrlMap urlMap = urlMapMapper.selectById(dto.getId());

        if (DateUtil.compare(urlMap.getExpireTime(), DateUtil.date()) < 0) {
            return ResultVO.error("不能修改已经过期的链接，请先激活");
        }
        // 如果是替换长URL，则要让原UrlMap失效，再平滑生成一个新的UrlMap，
        // 对用户而言，是无感知的。但是从统计学角度来看，这算得上一个版本升级，甚至是一个全新的操作
        if (ObjectUtil.notEqual(dto.getLongUrl().trim(), urlMap.getLongUrl())) {
            ShortUrlDeleteDTO shortUrlDeleteDTO = ShortUrlDeleteDTO.builder()
                    .shortUrl(urlMap.getShortUrl())
                    .compressionCode(urlMap.getCompressionCode())
                    .build();

            Map<String, String> domainMap = disassembleShortUrl(urlMap.getShortUrl());

            shortUrlDeleteDTO.setId(urlMap.getId());
            shortUrlDeleteDTO.setRequestId(dto.getRequestId());
            if (ObjectUtil.isNotEmpty(domainMap.get("protocol"))) {
                shortUrlDeleteDTO.setProtocol(domainMap.get("protocol"));
            }
            if (ObjectUtil.isNotEmpty(domainMap.get("bizType"))) {
                shortUrlDeleteDTO.setBizType(domainMap.get("bizType"));
            }
            if (ObjectUtil.isNotEmpty(domainMap.get("domain"))) {
                shortUrlDeleteDTO.setDomain(domainMap.get("domain"));
            }
            // 先删除
            if (StringUtils.equals(this.deleteShortUrlBiz(shortUrlDeleteDTO).getCode(), SUCCESS)) {
                // 再重新添加
                GenerateShortUrlDTO generateShortUrlDTO = GenerateShortUrlDTO.builder()
                        .requestId(dto.getRequestId())
                        .userId(dto.getUserId())
                        .bizType(shortUrlDeleteDTO.getBizType())
                        .domain(shortUrlDeleteDTO.getDomain())
                        .specialCompressionCode(urlMap.getCompressionCode())
                        .invalidateDate(urlMap.getExpireTime())
                        .longUrl(dto.getLongUrl())
                        .description(dto.getDescription())
                        .build();
                ResultVO<ShortUrlCreationVO> shortUrlBiz = this.createShortUrlBiz(generateShortUrlDTO);
                if (StringUtils.equals(shortUrlBiz.getCode(), SUCCESS)) {
                    ShortUrlQueryVO vo = new ShortUrlQueryVO();
                    QueryWrapper<UrlMap> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("short_url", shortUrlBiz.getData().getShortUrl());
                    queryWrapper.eq("user_id", dto.getUserId());
                    queryWrapper.eq("url_status", UrlMapStatus.AVAILABLE.getValue());
                    urlMap = urlMapMapper.selectOne(queryWrapper);
                    if (ObjectUtil.isNotEmpty(urlMap)) {
                        urlMap2ShortUrlQueryVO(urlMap);
                        return ResultVO.success(vo);
                    } else {
                        return ResultVO.error("编辑失败，某地方出了点问题");
                    }
                }
            }
        } else {
            // 只改描述，直接改即可
            // 改数据库
            if (ObjectUtil.notEqual(urlMap.getDescription(), dto.getDescription())) {
                UpdateWrapper<UrlMap> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", dto.getId());
                updateWrapper.set("description", dto.getDescription());
                urlMapMapper.update(null, updateWrapper);
                // 查结果
                urlMap = urlMapMapper.selectById(dto.getId());
                ShortUrlQueryVO vo = urlMap2ShortUrlQueryVO(urlMap);
                return ResultVO.success(vo);
            } else {
                return ResultVO.error("没有什么要修改的");
            }
        }
        return ResultVO.error("编辑失败");
    }

    /**
     * 解析shortUrl的URL，拆分到map中
     *
     * @param shortUrl 短URL
     * @return map
     */
    private Map<String, String> disassembleShortUrl(String shortUrl) {
        Map<String, String> res = new HashMap<>();
        try {
            URL url = new URL(shortUrl);
            String protocol = url.getProtocol();
            String host = url.getHost();
            String port = (url.getPort() != -1) ? String.valueOf(url.getPort()) : StrUtil.EMPTY;
            String domain;
            if (StringUtils.equals(port, StrUtil.EMPTY)) {
                domain = host;
            } else {
                domain = host + StrUtil.COLON + port;
            }
            String[] pathParts = url.getPath().split(StrUtil.SLASH);

            // 检查路径部分是否存在，然后获取路径的第一部分
            String bizType = pathParts.length > 1 ? pathParts[1] : StrUtil.EMPTY;
            res.put("protocol", protocol);
            res.put("domain", domain);
            res.put("bizType", bizType);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public ResultVO<List<ShortUrlQueryVO>> getShortUrlListBiz(ShortUrlQueryDTO dto) {
        // 分页配置
        Page<UrlMap> page = new Page<>(dto.getPage(), dto.getPageCount());

        List<ShortUrlQueryVO> vos = new ArrayList<>();
        QueryWrapper<UrlMap> wrapper = new QueryWrapper<>();

        assembleQueryShortUrlWrapper(wrapper, dto);
        wrapper.orderByDesc("expire_time");
        page = urlMapMapper.selectPage(page, wrapper);
        // 提取分页器内容，转为List
        for (UrlMap urlMap : page.getRecords()) {
            ShortUrlQueryVO vo = urlMap2ShortUrlQueryVO(urlMap);
            vos.add(vo);
        }
        if (!vos.isEmpty()) {
            return ResultVO.success(vos);
        }
        return ResultVO.error("未获取到信息");
    }

    @Override
    public ResultVO<ShortUrlDeleteVO> deleteShortUrlBiz(ShortUrlDeleteDTO dto) {

        UpdateWrapper<UrlMap> urlMapWrapper = new UpdateWrapper<>();
        UpdateWrapper<CompressionCode> compressionCodeWrapper = new UpdateWrapper<>();

        // 组装wrapper
        assembleDeleteUrlMapWrapper(urlMapWrapper, dto);
        assembleDeleteCompressionCodeWrapper(compressionCodeWrapper, dto);

        // 拿真实的UrlMap
        UrlMap urlMap = urlMapMapper.selectById(dto.getId());
        if (ObjectUtil.isNotNull(urlMap)) {
            // 时间校验
            if (DateUtil.compare(urlMap.getExpireTime(), DateUtil.date()) > 0) {
                // 删数据库
                if (urlMapMapper.update(null, urlMapWrapper) > 0 &&
                        compressionCodeMapper.update(null, compressionCodeWrapper) > 0) {
                    ShortUrlDeleteVO vo = new ShortUrlDeleteVO();
                    vo.setRequestId(dto.getRequestId());
                    vo.setId(dto.getId());
                    // 删缓存，直接设置立即过期，给listener监听到
                    if (redisUtil.set(ACCESS_CODE_STRING_PREFIX.getKey() + dto.getBizType() + COLON
                            + dto.getCompressionCode(), EMPTY_CACHE, INVALID_NOW)) {
                        return ResultVO.success(vo);
                    }
                }
            }
        }
        return ResultVO.error("主动下线短网址失败: " + dto.getShortUrl());
    }

    @Override
    public ResultVO<List<DomainConfSelectionVO>> getDomainConfForCreatingShortUrl() {
        List<DomainConfSelectionVO> vo = domainConfMapper.selectByDomainStatus();
        if (ObjectUtil.isNotEmpty(vo)) {
            return ResultVO.success(vo);
        }
        log.error("Failed to get the domain conf when try to create short url.");
        return ResultVO.error("获取域名信息失败");
    }

    @Override
    public ResultVO<Long> getShortUrlQueryCount(ShortUrlQueryDTO dto) {

        QueryWrapper<UrlMap> wrapper = new QueryWrapper<>();

        assembleQueryShortUrlWrapper(wrapper, dto);

        Long count = urlMapMapper.selectCount(wrapper);
        if (count > 0) {
            return ResultVO.success(count);
        }
        return ResultVO.error("无记录");
    }

    @Override
    public ResultVO<UrlMapLifeDateVO> getUrlMapById(UrlMapLifeDateDTO dto) {
        UrlMap urlMap = urlMapMapper.selectById(dto.getId());
        if (ObjectUtil.equals(urlMap.getUserId(), dto.getUserId())) {
            if (ObjectUtil.isNotEmpty(urlMap)) {
                UrlMapLifeDateVO vo = new UrlMapLifeDateVO();
                vo.setId(urlMap.getId());
                vo.setCreateTime(urlMap.getCreateTime());
                vo.setExpireTime(urlMap.getExpireTime());
                return ResultVO.success(vo);
            } else {
                return ResultVO.error("短网址记录不存在");
            }
        }
        return ResultVO.error("用户没有权限");
    }

    /**
     * 组装下线urlMap的wrapper
     *
     * @param wrapper wrapper
     * @param dto     dto
     */
    private void assembleDeleteUrlMapWrapper(UpdateWrapper<UrlMap> wrapper, ShortUrlDeleteDTO dto) {
        if (ObjectUtil.isNotEmpty(dto.getBizType())) {
            wrapper.eq("biz_type", dto.getBizType());
        }
        if (ObjectUtil.isNotEmpty(dto.getShortUrl())) {
            wrapper.eq("short_url", dto.getShortUrl());
        }
        if (ObjectUtil.isNotEmpty(dto.getCompressionCode())) {
            wrapper.eq("compression_code", dto.getCompressionCode());
        }
        if (ObjectUtil.isNotEmpty(dto.getId())) {
            wrapper.eq("id", dto.getId());
        }
        wrapper.set("url_status", UrlMapStatus.INVALID.getValue());
        // 设置当前时间为失效时间
        wrapper.set("expire_time", DateUtil.date());
    }

    /**
     * 组装下线compressionCode的wrapper
     *
     * @param wrapper wrapper
     * @param dto     dto
     */
    private void assembleDeleteCompressionCodeWrapper(UpdateWrapper<CompressionCode> wrapper, ShortUrlDeleteDTO dto) {
        if (ObjectUtil.isNotEmpty(dto.getCompressionCode())) {
            wrapper.eq("compression_code", dto.getCompressionCode());
        }
        if (StrUtil.isNotEmpty(dto.getDomain()) &&
                StrUtil.isNotEmpty(dto.getProtocol()) &&
                StrUtil.isNotEmpty(dto.getBizType())) {
            DomainConf domainConf = domainConfMapper.selectIdByDomainAndProtocol(dto.getDomain(),
                    dto.getProtocol(),
                    dto.getBizType());
            if (ObjectUtil.isNotEmpty(domainConf.getId())) {
                wrapper.eq("domain_conf_id", domainConf.getId());
            }
        }
        wrapper.set("code_status", CompressionCodeStatus.INVALID.getValue());
    }

    /**
     * 创建短链接
     *
     * @param dto 请求生成短链的传输数据
     * @return 短链创建结果
     */
    private String createShortUrl(GenerateShortUrlDTO dto) {
        // 判断短链域名和对应服务类别存在
        DomainConf domainConf = getDomainConf(dto.getDomain(), dto.getBizType());
        Assert.notNull(domainConf.getDomainValue(), String.format("域名不存在[%s]", dto.getDomain()));

        // 获取压缩码
        CompressionCode compressionCode;

        // 判断是随机生成压缩码还是用自定义的压缩码
        if (StringUtils.isEmpty(dto.getSpecialCompressionCode())) {
            compressionCode = getRandomAvailableCompressCode(domainConf.getId());
        } else {
            compressionCode = getSpecialAvailableCompressCode(
                    domainConf.getId(),
                    dto.getBizType(),
                    dto.getSpecialCompressionCode()
            );
        }

        Assert.isTrue(!BeanUtil.isEmpty(compressionCode) &&
                        CompressionCodeStatus.AVAILABLE.getValue().equals(compressionCode.getCodeStatus()),
                "压缩码不存在或者已经被使用");

        // 校验过期时间
        validateExpireTime(dto.getInvalidateDate());

        // 创建分布式锁
        DistributedLock lock = distributeLockFactory.provideDistributedLock(
                LockKey.CREATE_URL_MAP.getCode() + StrUtil.COLON
                        + dto.getDomain() + StrUtil.COLON
                        + dto.getBizType() + StrUtil.COLON
                        + compressionCode.getCompressionCode()
        );

        try {
            // 上锁
            if (lock.tryLock(LockKey.CREATE_URL_MAP.getWaitTime(), LockKey.CREATE_URL_MAP.getReleaseTime(), TimeUnit.MILLISECONDS)) {
                // 校验长链接
                String longUrl = dto.getLongUrl();
                Assert.isTrue(urlValidator.isValid(longUrl), String.format("链接[%s]非法", longUrl));

                String code = compressionCode.getCompressionCode();
                // 短链格式：（例）https://4im.cc/v/ABCD1234
                String shortUrl = String.format("%s://%s/%s/%s", domainConf.getProtocol(),
                        domainConf.getDomainValue(),
                        domainConf.getBizType(), code);

                UrlMap urlMap = UrlMap.builder()
                        .longUrl(dto.getLongUrl())
                        .shortUrl(shortUrl)
                        .compressionCode(code)
                        .domain_conf_id(domainConf.getId())
                        .urlStatus(CompressionCodeStatus.AVAILABLE.getValue())
                        .description(dto.getDescription())
                        .userId(dto.getUserId())
                        .bizType(dto.getBizType())
                        .expireTime(dto.getInvalidateDate())
                        .build();

                CompressionCode updater = CompressionCode.builder()
                        .codeStatus(USED.getValue())
                        .id(compressionCode.getId())
                        .build();

                // 事务，保存短链映射和更新压缩码状态
                transactionalService.saveUrlMapAndUpdateCompressCode(urlMap, updater);
                // 刷新缓存
                urlMapCacheManager.refreshUrlMapCache(urlMap);

                return shortUrl;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 校验短链过期时间
     *
     * @param expireTime 短链的过期时间
     */
    private void validateExpireTime(Date expireTime) {
        // LocalDateTime转换
        LocalDateTime localDateTime = LocalDateTimeUtil.of(expireTime);
        // 计算自定义的过期时长
        if (Long.parseLong(
                String.valueOf(localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() - DateUtil.currentSeconds())
        ) < 0) {
            throw new CreateShortUrlException(Integer.parseInt(EXPIRED_TIME_BEYOND_NOW_EXCEPTION.getCode()),
                    EXPIRED_TIME_BEYOND_NOW_EXCEPTION.getDescription() + DateUtil.formatDateTime(expireTime));
        }
    }

    /**
     * 获取域名信息
     *
     * @param domain  域名
     * @param bizType 服务类型
     * @return 域名信息
     */
    private DomainConf getDomainConf(String domain, String bizType) {
        DomainConf domainConf;

        // 先查缓存
        Object domainConfRedis = redisUtil.hGet(DOMAIN_CONF_HASH.getKey(), domain + PROTOCOL_SP + bizType);
        String jsonDomainConfRedis = JSONUtil.toJsonStr(domainConfRedis);
        domainConf = JSONUtil.toBean(jsonDomainConfRedis, DomainConf.class);

        if (!BeanUtil.isEmpty(domainConf)) {
            return domainConf;
        } else {
            // 缓存失效则查数据库
            domainConf = domainConfMapper.selectByDomain(domain, bizType);
            if (!BeanUtil.isEmpty(domainConf)) {
                // 写缓存
                redisUtil.hSet(DOMAIN_CONF_HASH.getKey(), domain + PROTOCOL_SP + bizType, domainConf);

                return domainConf;
            }
        }
        throw new SelectException(Integer.parseInt(SELECT_DOMAIN_CONF_EXCEPTION.getCode()),
                SELECT_DOMAIN_CONF_EXCEPTION.getDescription());
    }

    /**
     * 获取一个可用的压缩码
     *
     * @return 可用的压缩码
     */
    private CompressionCode getRandomAvailableCompressCode(Long domainConfId) {

        // 低于低水位则先发号
        if (redisUtil.sGetSetSize(COMPRESSION_CODE_POOL_SET.getKey()) < LWL) {
            generateBatchCompressionCodes();
        }
        String code = String.valueOf(redisUtil.sPop(COMPRESSION_CODE_POOL_SET.getKey()));

        Assert.notNull(code, String.format("短链池获取压缩码失败：[%s]", code));

        CompressionCode compressionCode = CompressionCode.builder()
                .compressionCode(code)
                .codeStatus(AVAILABLE.getValue())
                .domainConfId(domainConfId)
                .build();

        if (compressionCodeMapper.insert(compressionCode) > 0) {
            return compressionCode;
        } else {
            throw new InsertException(Integer.parseInt(INSERT_EXCEPTION.getCode()), INSERT_EXCEPTION.getDescription());
        }
    }

    /**
     * 根据指定的压缩码获取压缩码
     *
     * @return 可用的压缩码
     */
    private CompressionCode getSpecialAvailableCompressCode(Long domainConfId, String type, String code) {

        Long id;
        // 先查缓存
        if (!redisUtil.zSetHasItem(CacheKey.EXPIRE_ACCESS_CODE_ZSET_PREFIX.getKey() + type + COLON + ZSET, code)) {
            // 再查数据库
            if (ObjectUtil.isNull(id = compressionCodeMapper.hasCompressionCode(domainConfId, code))) {
                // 库里没有压缩码，则新建
                CompressionCode compressionCode = CompressionCode.builder()
                        .compressionCode(code)
                        .codeStatus(AVAILABLE.getValue())
                        .domainConfId(domainConfId)
                        .build();

                if (compressionCodeMapper.insert(compressionCode) > 0) {
                    return compressionCode;
                } else {
                    throw new InsertException(Integer.parseInt(INSERT_EXCEPTION.getCode()),
                            INSERT_EXCEPTION.getDescription());
                }
            } else {
                // 库里已有压缩码，但目前没人用（即原来有人用，但现在失效了）
                compressionCodeMapper.updateCodeStatusById(id, USED.getValue());
                return CompressionCode.builder()
                        .id(id)
                        .compressionCode(code)
                        .codeStatus(AVAILABLE.getValue())
                        .domainConfId(domainConfId)
                        .build();
            }
        } else {
            throw new CreateCompressionCodeException(Integer.parseInt(CREATE_COMPRESSION_CODE_EXCEPTION.getCode()),
                    CREATE_COMPRESSION_CODE_EXCEPTION.getDescription() + "type=" + type + ", code=" + code
            );
        }
    }

    /**
     * 批量生成压缩码
     */
    private void generateBatchCompressionCodes() {
        for (int i = 0; i < compressCodeBatch; i++) {
            // 生成整型序列
            Long sequence = sequenceGenerator.generate();

            // 压缩码进制转换（10 -> 63）
            String code = ConvertSequenceUtil.INSTANCE.encode63(sequence);

            log.info("[Generated the compression_code={}, Sequence={}]", code, sequence);
            // todo 改pipeline 以及 分布式锁？
            // 放入Redis压缩码池，要用再取
            redisUtil.sSet(COMPRESSION_CODE_POOL_SET.getKey(), code);
        }
    }
}