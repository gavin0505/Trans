package cc.forim.trans.shorturl.service.impl;

import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.shorturl.dao.CompressionCodeMapper;
import cc.forim.trans.shorturl.dao.DomainConfMapper;
import cc.forim.trans.shorturl.dao.UrlMapMapper;
import cc.forim.trans.shorturl.infra.constant.CompressionCodeStatus;
import cc.forim.trans.shorturl.infra.entity.CompressionCode;
import cc.forim.trans.shorturl.infra.entity.DomainConf;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import cc.forim.trans.shorturl.service.TransactionalService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static cc.forim.trans.shorturl.infra.constant.CacheKey.ACCESS_CODE_STRING_ID_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CacheKey.ACCESS_CODE_STRING_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CommonConstant.*;
import static cc.forim.trans.shorturl.infra.constant.CompressionCodeStatus.USED;
import static cc.forim.trans.shorturl.infra.constant.UrlMapStatus.AVAILABLE;

/**
 * 事务管理实现类
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Service("transactionalServiceImpl")
@Slf4j
public class TransactionalServiceImpl implements TransactionalService {

    @Resource(name = "compressionCodeMapper")
    private CompressionCodeMapper compressionCodeMapper;

    @Resource(name = "urlMapMapper")
    private UrlMapMapper urlMapMapper;

    @Resource(name = "domainConfMapper")
    private DomainConfMapper domainConfMapper;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUrlMapAndUpdateCompressCode(UrlMap urlMap, CompressionCode compressionCode) {
        compressionCodeMapper.updateByPrimaryKeySelective(compressionCode);
        urlMapMapper.insert(urlMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveShortUrlRenewalMessage(UrlMap urlMap) {
        String[] urlArr = urlMap.getShortUrl().split(StrUtil.SLASH);
        String domain = urlArr[urlArr.length - 3];
        String protocol = StrUtil.subBefore(urlArr[0], StrUtil.COLON, false);

        DomainConf domainConf = domainConfMapper.selectIdByDomainAndProtocol(domain, protocol, urlMap.getBizType());

        Assert.notNull(domainConf, "域名配置不存在");

        Long compressionCodeId = compressionCodeMapper.selectIdByCodeAndDomainConfId(urlMap.getCompressionCode(), domainConf.getId());

        compressionCodeMapper.updateCodeStatusById(compressionCodeId, USED.getValue());
        urlMapMapper.updateById(urlMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteExpireUrlMap(String type, String setKey) {
        Set<Object> sets = redisUtil.sGet(setKey);
        log.info("Detect expired keys, type={}, codes=[{}].", type, sets);

        List<Long> expiredUrlMapIds = Lists.newArrayList();
        List<String> expiredCodes = Lists.newArrayList();
        log.info("START to clean the expired keys.");

        if (!sets.isEmpty()) {
            for (Object set : sets) {
                // 删缓存
                String expiredKey = String.valueOf(set);
                redisUtil.setDel(setKey, expiredKey);

                String[] keySegments = expiredKey.split(COLON);
                String urlMapIdKey = ACCESS_CODE_STRING_ID_PREFIX.getKey() + type + COLON + expiredKey;
                int codeIndex = keySegments.length - 1;
                Long urlMapId = Long.parseLong(String.valueOf(redisUtil.get(urlMapIdKey)));
                // 加过期urlMap的id和过去压缩码
                expiredUrlMapIds.add(urlMapId);
                expiredCodes.add(keySegments[codeIndex]);
                // 删过期键存的urlMap的id
                redisUtil.del(urlMapIdKey);
            }

            // 数据库设置urlMap失效（批量）
            if (urlMapMapper.expiredByIds(expiredUrlMapIds) > 0) {
                log.info("The invalidation operation of url_map data from Database is SUCCESS.");
            } else {
                log.warn("FAILED to invalidate the url_map data from Database.");
                // todo 抛异常
            }
            // 数据库设置压缩码失效
            if (compressionCodeMapper.expiredByCodes(expiredCodes) > 0) {
                log.info("The invalidation operation of compression_code data from Database is SUCCESS.");
            } else {
                log.warn("FAILED to invalidate the compression_code data from Database.");
                // todo 抛异常
            }
            log.info("END to clean the expired keys.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteExpireUrlMapCompensation(Long domainConfId, String type, Date
            date, UpdateWrapper<UrlMap> urlMapUpdateWrapper) {
        //todo
        log.info("现在开始执行补偿删除UrlMap");
        List<String> compressionCodes = urlMapMapper.selectExpiredCompressionCodeByStatusAndBizTypeAndDomain(date,
                AVAILABLE.getValue(),
                type,
                domainConfId);
        log.info("检测到domainId={}，服务类型={}，待补偿删除压缩码={}", domainConfId, type, compressionCodes);
        // 让过期的UrlMap失效
        urlMapMapper.update(null, urlMapUpdateWrapper);

        for (String compressionCode : compressionCodes) {
            // Redis中的key
            String expiredKey = ACCESS_CODE_STRING_PREFIX.getKey() + type + COLON
                    + compressionCode;

            // 让过期的压缩码失效
            UpdateWrapper<CompressionCode> codeUpdateWrapper = new UpdateWrapper<>();
            codeUpdateWrapper.eq("domain_conf_id", domainConfId);
            codeUpdateWrapper.eq("compression_code", compressionCode);
            codeUpdateWrapper.eq("deleted", EXIST);
            codeUpdateWrapper.set("code_status", CompressionCodeStatus.INVALID.getValue());
            compressionCodeMapper.update(null, codeUpdateWrapper);

            // 设置空缓存
            redisUtil.set(expiredKey, EMPTY_CACHE, SEVEN_DAY_SECONDS);
            // 删过期键存的urlMap的id
            String urlMapIdKey = ACCESS_CODE_STRING_ID_PREFIX.getKey() + type + COLON + expiredKey;
            redisUtil.del(urlMapIdKey);

        }
        log.info("补偿删除UrlMap完成");

    }
}