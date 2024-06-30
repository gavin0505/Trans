package cc.forim.trans.statistics.cron;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.statistics.dao.UrlMapMapper;
import cc.forim.trans.statistics.infra.domain.RequestContext;
import cc.forim.trans.statistics.infra.dto.TPlusOneDTO;
import cc.forim.trans.statistics.infra.entity.UrlMap;
import cc.forim.trans.statistics.infra.utils.RequestContextHolder;
import cc.forim.trans.statistics.infra.vo.TPlusOneVO;
import cc.forim.trans.statistics.service.StatisticsService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

import static cc.forim.trans.common.enums.UserTypeConstant.SUPER_ADMIN;
import static cc.forim.trans.statistics.infra.common.TimeCommon.T_PLUS_ONE_CACHE_KEEP_ALIVE;

/**
 * T+1数据缓存定时任务
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Component
@Slf4j
@RefreshScope
@Data
@EnableTransactionManagement
@EnableScheduling
public class TPlusOneCacheJob {

    @Resource(name = "urlMapMapper")
    private UrlMapMapper urlMapMapper;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Resource(name = "statisticsServiceImpl")
    private StatisticsService statisticsService;


    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void cacheUrlMapDaily() {
        log.info("开始执行定时增加T+1统计信息到缓存");
        QueryWrapper<UrlMap> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);

        Long count = urlMapMapper.selectCount(queryWrapper);

        Date endDate = DateUtil.date();

        RequestContext context = new RequestContext();

        context.setTag(1);

        for (int i = 1; i <= count; i++) {
            log.info("开始执行id={}的定时增加T+1统计信息到缓存", i);
            context.setUserType(SUPER_ADMIN);
            context.setTraceId(System.currentTimeMillis() + "-" +
                    UUID.randomUUID());
            RequestContextHolder.setContext(context);
            UrlMap urlMap = urlMapMapper.selectById(i);
            TPlusOneDTO dto = new TPlusOneDTO();
            dto.setStartDate(urlMap.getCreateTime());
            dto.setId(urlMap.getId());
            dto.setEndDate(endDate);
            ResultVO<TPlusOneVO> tPlusOne = statisticsService.getTPlusOne(dto);
            if (ObjectUtil.isNotEmpty(tPlusOne)) {
                String key = "trans:sta:t_p_one:" + i;
                redisUtil.set(key, tPlusOne.getData(), T_PLUS_ONE_CACHE_KEEP_ALIVE);
                log.info("完成id={}的定时增加T+1统计信息到缓存, data={}", i, tPlusOne.getData());
            }
        }
        RequestContextHolder.clear();
        log.info("结束定时增加T+1统计信息到缓存");
    }
}
