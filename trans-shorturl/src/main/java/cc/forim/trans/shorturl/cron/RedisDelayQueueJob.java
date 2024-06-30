package cc.forim.trans.shorturl.cron;

import cc.forim.trans.common.lock.DistributeLockFactory;
import cc.forim.trans.common.lock.DistributedLock;
import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.shorturl.dao.CompressionCodeMapper;
import cc.forim.trans.shorturl.dao.UrlMapMapper;
import cc.forim.trans.shorturl.infra.constant.LockKey;
import cc.forim.trans.shorturl.infra.constant.UrlMapStatus;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import cc.forim.trans.shorturl.service.TransactionalService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cc.forim.trans.shorturl.infra.constant.CacheKey.EXPIRE_ACCESS_CODE_SET_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CommonConstant.*;
import static cc.forim.trans.shorturl.infra.constant.UrlMapStatus.AVAILABLE;

/**
 * Redis 延迟队列的任务
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Component
@Slf4j
@RefreshScope
@ConfigurationProperties(prefix = "trans")
@Data
@EnableTransactionManagement
public class RedisDelayQueueJob {

    /**
     * 补偿数据门限
     */
    private static final int COMPENSATION_THRESHOLD = 100000;

    /**
     * 最小score
     */
    private final static double MIN_SCORE = 0.0;

    @Resource(name = "transactionalServiceImpl")
    private TransactionalService transactionalService;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    private String[] serviceType;

    @Resource
    private UrlMapMapper urlMapMapper;

    @Resource
    private CompressionCodeMapper compressionCodeMapper;

    @Resource(name = "distributeLockFactory")
    private DistributeLockFactory distributeLockFactory;

    /**
     * 定时删除过期键
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void deleteExpireUrlMap() {
        // 创建分布式锁
        DistributedLock lock = distributeLockFactory.provideDistributedLock(LockKey.DELETE_EXPIRED_URL_MAP.getCode());
        try {
            // 上锁
            if (lock.tryLock(LockKey.DELETE_EXPIRED_URL_MAP.getWaitTime(),
                    LockKey.DELETE_EXPIRED_URL_MAP.getReleaseTime(),
                    TimeUnit.MILLISECONDS)) {
                for (String type : serviceType) {
                    String setKey = EXPIRE_ACCESS_CODE_SET_PREFIX.getKey() + type + COLON + SET;
                    Long count = redisUtil.sGetSetSize(setKey);
                    if (count > 0) {
                        transactionalService.deleteExpireUrlMap(type, setKey);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 定时删除过期键的补偿任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void deleteExpireUrlMapCompensation() {
        // 创建分布式锁
        DistributedLock lock = distributeLockFactory.provideDistributedLock(
                LockKey.DELETE_EXPIRED_URL_MAP_COMPENSATION.getCode()
        );
        try {
            // 上锁
            if (lock.tryLock(LockKey.DELETE_EXPIRED_URL_MAP_COMPENSATION.getWaitTime(),
                    LockKey.DELETE_EXPIRED_URL_MAP_COMPENSATION.getReleaseTime(),
                    TimeUnit.MILLISECONDS)) {

                // 数据库扫表，先探测问题记录，有的话再补偿
                Date now = DateUtil.date();
                // 时间设置为当前时间-15s，被视为需要补偿
                Date suspectTime = DateUtil.offsetSecond(now, -15);
                // 找到需要补偿的域名的id
                List<Long> compensationDomainIdList = urlMapMapper.selectInvalidUpdateUrlMapDomainConfId(suspectTime,
                        AVAILABLE.getValue());

                if (ObjectUtil.isNotEmpty(compensationDomainIdList)) {
                    for (Long domainConfId : compensationDomainIdList) {
                        for (String type : serviceType) {
                            QueryWrapper<UrlMap> queryWrapper = getCompensationQueryWrapper(suspectTime, type, domainConfId);
                            UpdateWrapper<UrlMap> updateWrapper = getCompensationUpdateWrapper(suspectTime, type, domainConfId);
                            if (urlMapMapper.selectCount(queryWrapper) > 0) {
                                transactionalService.deleteExpireUrlMapCompensation(domainConfId, type, suspectTime, updateWrapper);
                            }
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 组装补偿删除UrlMap的查询条件
     *
     * @param suspectTime  失效决断时间，这个失效时间内还存在没更新失效状态的UrlMap，被认为数据不一致
     * @param type         业务类型
     * @param domainConfId 域名配置id
     */
    private QueryWrapper<UrlMap> getCompensationQueryWrapper(Date suspectTime, String type, Long domainConfId) {
        QueryWrapper<UrlMap> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expire_time", suspectTime);
        queryWrapper.eq("biz_type", type);
        queryWrapper.eq("domain_conf_id", domainConfId);
        queryWrapper.eq("deleted", EXIST);

        return queryWrapper;
    }

    /**
     * 组装补偿删除UrlMap的更新条件
     *
     * @param suspectTime  失效决断时间，这个失效时间内还存在没更新失效状态的UrlMap，被认为数据不一致
     * @param type         业务类型
     * @param domainConfId 域名配置id
     */
    private UpdateWrapper<UrlMap> getCompensationUpdateWrapper(Date suspectTime, String type, Long domainConfId) {
        UpdateWrapper<UrlMap> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lt("expire_time", suspectTime);
        updateWrapper.eq("biz_type", type);
        updateWrapper.eq("domain_conf_id", domainConfId);
        updateWrapper.eq("deleted", EXIST);
        updateWrapper.set("url_status", UrlMapStatus.INVALID.getValue());

        return updateWrapper;
    }
}
