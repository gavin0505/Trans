package cc.forim.trans.statistics.service.impl;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.statistics.dao.AnalysisMapper;
import cc.forim.trans.statistics.dao.UrlMapMapper;
import cc.forim.trans.statistics.infra.domain.*;
import cc.forim.trans.statistics.infra.dto.TPlusOneDTO;
import cc.forim.trans.statistics.infra.dto.TimeAndGeoSearchDTO;
import cc.forim.trans.statistics.infra.entity.UrlMap;
import cc.forim.trans.statistics.infra.utils.RequestContextHolder;
import cc.forim.trans.statistics.infra.vo.*;
import cc.forim.trans.statistics.service.StatisticsService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static cc.forim.trans.common.enums.UserTypeConstant.USER;
import static cc.forim.trans.statistics.infra.common.TimeCommon.T_PLUS_ONE_CACHE_KEEP_ALIVE;

/**
 * @author Gavin Zhang
 * @version V1.0
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource(name = "analysisMapper")
    private AnalysisMapper analysisMapper;

    @Resource(name = "urlMapMapper")
    private UrlMapMapper urlMapMapper;

    @Resource(name = "clickHouseTaskExecutor")
    private ThreadPoolExecutor clickHouseTaskExecutor;

    /**
     * 按省份搜索的tag
     */
    private static final int GEO_PROVINCE = 1;

    /**
     * 按城市搜索的tag
     */
    private static final int GEO_CITY = 2;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Override
    public ResultVO<TPlusOneVO> getTPlusOne(TPlusOneDTO dto) {

        RequestContext requestContext = RequestContextHolder.getContext();
        UrlMap urlMap;

        if (ObjectUtil.isNotEmpty(dto.getId())) {
            urlMap = urlMapMapper.selectById(dto.getId());
        } else {
            return ResultVO.error("未能获取对应的短网址映射信息");
        }

        if (ObjectUtil.equals(requestContext.getUserType(), USER)) {
            if (ObjectUtil.notEqual(requestContext.getUserId(), urlMap.getUserId())) {
                return ResultVO.error("用户身份与对应映射信息不匹配");
            }
        }
        if (ObjectUtil.isNotNull(dto.getStartDate())) {
            Date createTime = DateUtil.beginOfDay(urlMap.getCreateTime());
            Date startTime = DateUtil.beginOfDay(dto.getStartDate());
            if (DateUtil.compare(startTime, createTime) >= 0) {
                dto.setStartDate(startTime);
            } else {
                return ResultVO.error("此时短网址映射尚未生成");
            }
        } else {
            dto.setStartDate(DateUtil.beginOfDay(urlMap.getCreateTime()));
        }

        if (ObjectUtil.isNotNull(dto.getEndDate())) {
            Date createTime = DateUtil.beginOfDay(urlMap.getCreateTime());
            Date endDate = DateUtil.beginOfDay(dto.getEndDate());
            if (DateUtil.compare(createTime, endDate) <= 0) {
                dto.setEndDate(endDate);
            } else {
                return ResultVO.error("此时短网址映射尚未生成");
            }
        } else {
            //todo 到底该不该设置当前时间为endDate？
            dto.setEndDate(DateUtil.beginOfDay(DateUtil.date()));
        }

        log.info("TraceId={}, Now is executing t_plus_one query, url_map_id={}",
                requestContext.getTraceId(), dto.getId());

        // todo 拼接缓存key，先查缓存
        String cacheKey = "trans:sta:t_p_one:" + urlMap.getId();
        Object obj = redisUtil.get(cacheKey);
        if (ObjectUtil.isNotEmpty(obj) && ObjectUtil.notEqual(requestContext.getTag(), 1)) {
            String jsonStr = JSONUtil.toJsonStr(obj);
            TPlusOneVO tPlusOneVO = JSONUtil.toBean(jsonStr, TPlusOneVO.class);
            return ResultVO.success(tPlusOneVO);
        }

        // 声明各子查询
        CompletableFuture<TotalCountVO> totalCountFuture = getTPlusOneTotalCount(dto);
        CompletableFuture<SevenDaysCountVO> sevenDaysCountFuture = getTPlusOneSevenDaysCount(dto);
        CompletableFuture<XWeeksCountVO> xWeeksCountFuture = getTPlusOneXWeeksCount(dto, 8);
        CompletableFuture<EveryHourCountVO> hourSegmentCountFuture = getTPlusOneHourSegmentCount(dto);
        CompletableFuture<EveryProvinceCountVO> everyProvinceCountFuture = getTPlusOneEveryProvinceCount(dto);
        CompletableFuture<TopXCityCountVO> topXCityCountFuture = getTPlusOneTopXCityCount(dto, 10);
        CompletableFuture<EveryBrowserCountVO> everyBrowserCountFuture = getTPlusOneEveryBrowserCount(dto);
        CompletableFuture<EveryOSCountVO> everyOSCountFuture = getTPlusOneEveryOSCount(dto);
        CompletableFuture<PhoneAndPCCountVO> phoneAndPCCountFuture = getTPlusOnePhoneAndPCPercentage(dto);
        CompletableFuture<EveryISPCountVO> ispCountFuture = getTPlusOneEveryISPCount(dto);

        // 合并所有任务，并等待
        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(totalCountFuture, sevenDaysCountFuture, xWeeksCountFuture, hourSegmentCountFuture,
                        everyProvinceCountFuture, topXCityCountFuture, everyBrowserCountFuture, everyOSCountFuture,
                        phoneAndPCCountFuture, ispCountFuture);
        allFutures.join();

        TotalCountVO totalCountVO = totalCountFuture.join();
        // 拼装T+1的结果
        TPlusOneVO tPlusOneVO = new TPlusOneVO();
        tPlusOneVO.setTotalCountVO(totalCountVO);
        // 拼装T+1的7日内统计结果
        SevenDaysCountVO sevenDaysCountVO = sevenDaysCountFuture.join();
        tPlusOneVO.setSevenDaysCountVO(sevenDaysCountVO);
        // 拼装T+1的近8周的统计结果
        XWeeksCountVO xWeeksCountVO = xWeeksCountFuture.join();
        tPlusOneVO.setXWeeksCountVO(xWeeksCountVO);
        // 拼装T+1每个小时统计访问量结果
        EveryHourCountVO everyHourCountVO = hourSegmentCountFuture.join();
        tPlusOneVO.setEveryHourCountVO(everyHourCountVO);
        // 拼装T+1每个省访问量结果
        EveryProvinceCountVO everyProvinceCountVO = everyProvinceCountFuture.join();
        tPlusOneVO.setEveryProvinceCountVO(everyProvinceCountVO);
        //拼装T+1的top10访问量城市的结果
        TopXCityCountVO topXCityCountVO = topXCityCountFuture.join();
        tPlusOneVO.setTopXCityCountVO(topXCityCountVO);
        // 拼装T+1的每个浏览器的访问量结果
        EveryBrowserCountVO everyBrowserCountVO = everyBrowserCountFuture.join();
        tPlusOneVO.setEveryBrowserCountVO(everyBrowserCountVO);
        // 拼装T+1的每个操作系统的访问量结果
        EveryOSCountVO everyOSCountVO = everyOSCountFuture.join();
        tPlusOneVO.setEveryOSCountVO(everyOSCountVO);
        // 拼装T+1的手机和PC的访问量结果
        PhoneAndPCCountVO phoneAndPCCountVO = phoneAndPCCountFuture.join();
        tPlusOneVO.setPhoneAndPCCountVO(phoneAndPCCountVO);
        // 拼装T+1的ISP访问量结果
        EveryISPCountVO everyISPCountVO = ispCountFuture.join();
        tPlusOneVO.setEveryISPCountVO(everyISPCountVO);

        redisUtil.set(cacheKey, tPlusOneVO, T_PLUS_ONE_CACHE_KEEP_ALIVE);

        log.info("TraceId={}, Now is finish t_plus_one query, url_map_id={}",
                requestContext.getTraceId(), dto.getId());
        return ResultVO.success(tPlusOneVO);
    }

    @Override
    public ResultVO<AllByGeoAndTimeSpanVO> getTFromGeoAndTimeSpan(TimeAndGeoSearchDTO dto, Long userId) {

        RequestContext requestContext = RequestContextHolder.getContext();
        UrlMap urlMap;

        if (ObjectUtil.isNotEmpty(dto.getId())) {
            urlMap = urlMapMapper.selectById(dto.getId());
        } else {
            return ResultVO.error("未能获取对应的短网址映射信息");
        }

        if (ObjectUtil.equals(requestContext.getUserType(), USER)) {
            if (ObjectUtil.notEqual(requestContext.getUserId(), urlMap.getUserId())) {
                return ResultVO.error("用户身份与对应映射信息不匹配");
            }
        }

        AllByGeoAndTimeSpanVO allByGeoAndTimeSpanVO = null;
        // 特判
        if (ObjectUtil.equals(dto.getGeoType(), GEO_PROVINCE)) {
            // 走省查询
            allByGeoAndTimeSpanVO = getTimeSpanByProvince(dto);
        } else if (ObjectUtil.equals(dto.getGeoType(), GEO_CITY)) {
            // 走市查询
            allByGeoAndTimeSpanVO = getTimeSpanByCity(dto);
        }

        // 返回结果
        if (ObjectUtil.isNotEmpty(allByGeoAndTimeSpanVO)) {
            return ResultVO.success(allByGeoAndTimeSpanVO);
        }
        return ResultVO.error("查询失败，请检查参数");
    }

    @Override
    public ResultVO<PanelGroupVO> getPanelGroup(Long userId) {
        CompletableFuture<Long> urlMapCountFuture = getUrlMapCountByUserId(userId);
        CompletableFuture<Long> urlMapVisitCountFuture = getUrlMapVisitCountByUserId(userId);
        CompletableFuture<Long> todayUrlMapVisitCountFuture = getTodayUrlMapVisitCountByUserId(userId);
        CompletableFuture<Long> todayUrlMapVisitIpCountFuture = getTodayUrlMapVisitIpCountByUserId(userId);

        CompletableFuture<SevenDaysCountVO> sevenDaysCountFuture = getTSevenDaysCountByUserId(userId);

        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(urlMapCountFuture, urlMapVisitCountFuture, todayUrlMapVisitCountFuture, todayUrlMapVisitIpCountFuture, sevenDaysCountFuture);
        allFutures.join();

        PanelGroupVO vo = new PanelGroupVO();
        vo.setUrlMapCount(urlMapCountFuture.join());
        vo.setUrlMapVisitCount(urlMapVisitCountFuture.join());
        vo.setTodayUrlMapVisitCount(todayUrlMapVisitCountFuture.join());
        vo.setTodayUrlMapVisitIpCount(todayUrlMapVisitIpCountFuture.join());

        SevenDaysCountVO sevenDaysCountVO = sevenDaysCountFuture.join();
        vo.setSevenDaysCountVO(sevenDaysCountVO);

        return ResultVO.success(vo);
    }

    private CompletableFuture<Long> getUrlMapCountByUserId(Long id) {
        QueryWrapper<UrlMap> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        return CompletableFuture.supplyAsync(() ->
                urlMapMapper.selectCount(queryWrapper), clickHouseTaskExecutor);
    }

    private CompletableFuture<Long> getUrlMapVisitCountByUserId(Long id) {
        return CompletableFuture.supplyAsync(() ->
                analysisMapper.getUrlMapVisitCountByUserId(id), clickHouseTaskExecutor);
    }

    private CompletableFuture<Long> getTodayUrlMapVisitCountByUserId(Long id) {
        return CompletableFuture.supplyAsync(() ->
                analysisMapper.getTodayUrlMapVisitCountById(id), clickHouseTaskExecutor);
    }

    private CompletableFuture<Long> getTodayUrlMapVisitIpCountByUserId(Long id) {

        return CompletableFuture.supplyAsync(() ->
                analysisMapper.getTodayUrlMapVisitIpCountById(id), clickHouseTaskExecutor);
    }

    private AllByGeoAndTimeSpanVO getTimeSpanByProvince(TimeAndGeoSearchDTO dto) {
        // 声明各子查询
        CompletableFuture<TotalCountVO> totalCountFuture = getTimeRangeByProvinceTotalCount(dto);
        CompletableFuture<SevenDaysCountVO> sevenDaysCountFuture = getTimeRangeByProvinceSevenDaysCount(dto);
        CompletableFuture<XWeeksCountVO> xWeeksCountFuture = getTimeRangeByProvinceXWeeksCount(dto, 8);
        CompletableFuture<EveryHourCountVO> hourSegmentCountFuture = getTimeRangeByProvinceHourSegmentCount(dto);
        CompletableFuture<EveryProvinceCountVO> everyProvinceCountFuture = getTimeRangeByProvinceEveryProvinceCount(dto);
        CompletableFuture<TopXCityCountVO> topXCityCountFuture = getTimeRangeByProvinceTopXCityCount(dto, 10);
        CompletableFuture<EveryBrowserCountVO> everyBrowserCountFuture = getTimeRangeByProvinceEveryBrowserCount(dto);
        CompletableFuture<EveryOSCountVO> everyOSCountFuture = getTimeRangeByProvinceEveryOSCount(dto);
        CompletableFuture<PhoneAndPCCountVO> phoneAndPCCountFuture = getTimeRangeByProvincePhoneAndPCPercentage(dto);
        CompletableFuture<EveryISPCountVO> ispCountFuture = getTimeRangeByProvinceEveryISPCount(dto);

        // 合并所有任务，并等待
        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(totalCountFuture, sevenDaysCountFuture, xWeeksCountFuture, hourSegmentCountFuture,
                        everyProvinceCountFuture, topXCityCountFuture, everyBrowserCountFuture, everyOSCountFuture,
                        phoneAndPCCountFuture, ispCountFuture);
        allFutures.join();

        TotalCountVO totalCountVO = totalCountFuture.join();
        // 拼装省份的结果
        AllByGeoAndTimeSpanVO allByGeoAndTimeSpanVO = new AllByGeoAndTimeSpanVO();
        allByGeoAndTimeSpanVO.setTotalCountVO(totalCountVO);
        // 拼装省份的7日内统计结果
        SevenDaysCountVO sevenDaysCountVO = sevenDaysCountFuture.join();
        allByGeoAndTimeSpanVO.setSevenDaysCountVO(sevenDaysCountVO);
        // 拼装省份的近8周的统计结果
        XWeeksCountVO xWeeksCountVO = xWeeksCountFuture.join();
        allByGeoAndTimeSpanVO.setXWeeksCountVO(xWeeksCountVO);
        // 拼装省份每个小时统计访问量结果
        EveryHourCountVO everyHourCountVO = hourSegmentCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryHourCountVO(everyHourCountVO);
        // 拼装省份每个省访问量结果
        EveryProvinceCountVO everyProvinceCountVO = everyProvinceCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryProvinceCountVO(everyProvinceCountVO);
        //拼装省份的top10访问量城市的结果
        TopXCityCountVO topXCityCountVO = topXCityCountFuture.join();
        allByGeoAndTimeSpanVO.setTopXCityCountVO(topXCityCountVO);
        // 拼装省份的每个浏览器的访问量结果
        EveryBrowserCountVO everyBrowserCountVO = everyBrowserCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryBrowserCountVO(everyBrowserCountVO);
        // 拼装省份的每个操作系统的访问量结果
        EveryOSCountVO everyOSCountVO = everyOSCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryOSCountVO(everyOSCountVO);
        // 拼装省份的手机和PC的访问量结果
        PhoneAndPCCountVO phoneAndPCCountVO = phoneAndPCCountFuture.join();
        allByGeoAndTimeSpanVO.setPhoneAndPCCountVO(phoneAndPCCountVO);
        // 拼装省份的ISP访问量结果
        EveryISPCountVO everyISPCountVO = ispCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryISPCountVO(everyISPCountVO);

        return allByGeoAndTimeSpanVO;
    }

    private AllByGeoAndTimeSpanVO getTimeSpanByCity(TimeAndGeoSearchDTO dto) {
        // 声明各子查询
        CompletableFuture<TotalCountVO> totalCountFuture = getTimeRangeByCityTotalCount(dto);
        CompletableFuture<SevenDaysCountVO> sevenDaysCountFuture = getTimeRangeByCitySevenDaysCount(dto);
        CompletableFuture<XWeeksCountVO> xWeeksCountFuture = getTimeRangeByCityXWeeksCount(dto, 8);
        CompletableFuture<EveryHourCountVO> hourSegmentCountFuture = getTimeRangeByCityHourSegmentCount(dto);
        CompletableFuture<EveryProvinceCountVO> everyProvinceCountFuture = getTimeRangeByCityEveryProvinceCount(dto);
        CompletableFuture<TopXCityCountVO> topXCityCountFuture = getTimeRangeByCityTopXCityCount(dto, 10);
        CompletableFuture<EveryBrowserCountVO> everyBrowserCountFuture = getTimeRangeByCityEveryBrowserCount(dto);
        CompletableFuture<EveryOSCountVO> everyOSCountFuture = getTimeRangeByCityEveryOSCount(dto);
        CompletableFuture<PhoneAndPCCountVO> phoneAndPCCountFuture = getTimeRangeByCityPhoneAndPCPercentage(dto);
        CompletableFuture<EveryISPCountVO> ispCountFuture = getTimeRangeByCityEveryISPCount(dto);

        // 合并所有任务，并等待
        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(totalCountFuture, sevenDaysCountFuture, xWeeksCountFuture, hourSegmentCountFuture,
                        everyProvinceCountFuture, topXCityCountFuture, everyBrowserCountFuture, everyOSCountFuture,
                        phoneAndPCCountFuture, ispCountFuture);
        allFutures.join();

        TotalCountVO totalCountVO = totalCountFuture.join();
        // 拼装省份的结果
        AllByGeoAndTimeSpanVO allByGeoAndTimeSpanVO = new AllByGeoAndTimeSpanVO();
        allByGeoAndTimeSpanVO.setTotalCountVO(totalCountVO);
        // 拼装省份的7日内统计结果
        SevenDaysCountVO sevenDaysCountVO = sevenDaysCountFuture.join();
        allByGeoAndTimeSpanVO.setSevenDaysCountVO(sevenDaysCountVO);
        // 拼装省份的近8周的统计结果
        XWeeksCountVO xWeeksCountVO = xWeeksCountFuture.join();
        allByGeoAndTimeSpanVO.setXWeeksCountVO(xWeeksCountVO);
        // 拼装省份每个小时统计访问量结果
        EveryHourCountVO everyHourCountVO = hourSegmentCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryHourCountVO(everyHourCountVO);
        // 拼装省份每个省访问量结果
        EveryProvinceCountVO everyProvinceCountVO = everyProvinceCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryProvinceCountVO(everyProvinceCountVO);
        //拼装省份的top10访问量城市的结果
        TopXCityCountVO topXCityCountVO = topXCityCountFuture.join();
        allByGeoAndTimeSpanVO.setTopXCityCountVO(topXCityCountVO);
        // 拼装省份的每个浏览器的访问量结果
        EveryBrowserCountVO everyBrowserCountVO = everyBrowserCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryBrowserCountVO(everyBrowserCountVO);
        // 拼装省份的每个操作系统的访问量结果
        EveryOSCountVO everyOSCountVO = everyOSCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryOSCountVO(everyOSCountVO);
        // 拼装省份的手机和PC的访问量结果
        PhoneAndPCCountVO phoneAndPCCountVO = phoneAndPCCountFuture.join();
        allByGeoAndTimeSpanVO.setPhoneAndPCCountVO(phoneAndPCCountVO);
        // 拼装省份的ISP访问量结果
        EveryISPCountVO everyISPCountVO = ispCountFuture.join();
        allByGeoAndTimeSpanVO.setEveryISPCountVO(everyISPCountVO);

        return allByGeoAndTimeSpanVO;
    }

    private CompletableFuture<TotalCountVO> getTPlusOneTotalCount(TPlusOneDTO dto) {
        // T+1
        CompletableFuture<Long> tPlusOneFuture = CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneTotalCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(count -> count);
        // T+2
        CompletableFuture<Long> tPlusTwoFuture = CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneTotalCount(dto.getStartDate(), DateUtil.offsetDay(dto.getEndDate(), -1)
                                , dto.getId()), clickHouseTaskExecutor)
                .thenApply(count -> count);
        // 整合两个计算而已
        return tPlusOneFuture.thenCombine(tPlusTwoFuture, (count, lastDayCount) -> {
            TotalCountVO vo = new TotalCountVO();
            vo.setCount(count);
            vo.setRaiseCount(count - lastDayCount);
            vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
            return vo;
        });
    }

    private CompletableFuture<TotalCountVO> getTimeRangeByProvinceTotalCount(TimeAndGeoSearchDTO dto) {
        // T+1
        CompletableFuture<Long> tPlusOneFuture = CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceTotalCount(
                                dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor
                )
                .thenApply(count -> count);
        // T+2
        CompletableFuture<Long> tPlusTwoFuture = CompletableFuture.supplyAsync(() ->
                                analysisMapper.selectTimeRangeByProvinceTotalCount(
                                        dto.getStartDate(), DateUtil.offsetDay(dto.getEndDate(), -1), dto.getId(), dto.getGeos()),
                        clickHouseTaskExecutor)
                .thenApply(count -> count);
        // 整合两个计算而已
        return tPlusOneFuture.thenCombine(tPlusTwoFuture, (count, lastDayCount) -> {
            TotalCountVO vo = new TotalCountVO();
            vo.setCount(count);
            vo.setRaiseCount(count - lastDayCount);
            vo.setEndDate(dto.getEndDate());
            return vo;
        });
    }

    private CompletableFuture<TotalCountVO> getTimeRangeByCityTotalCount(TimeAndGeoSearchDTO dto) {
        // T+1
        CompletableFuture<Long> tPlusOneFuture = CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityTotalCount(
                                dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(count -> count);
        // T+2
        CompletableFuture<Long> tPlusTwoFuture = CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityTotalCount(dto.getStartDate(), DateUtil.offsetDay(dto.getEndDate(), -1)
                                , dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(count -> count);
        // 整合两个计算而已
        return tPlusOneFuture.thenCombine(tPlusTwoFuture, (count, lastDayCount) -> {
            TotalCountVO vo = new TotalCountVO();
            vo.setCount(count);
            vo.setRaiseCount(count - lastDayCount);
            vo.setEndDate(dto.getEndDate());
            return vo;
        });
    }

    private CompletableFuture<SevenDaysCountVO> getTSevenDaysCountByUserId(Long userId) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.getTSevenDaysCountByUserId(userId), clickHouseTaskExecutor)
                .thenApply(daysRecord -> {
                    SevenDaysCountVO vo = new SevenDaysCountVO();
                    if (ObjectUtil.isNotEmpty(daysRecord)) {
                        vo.setCount(daysRecord.stream().mapToLong(DayRecord::getCount).sum());
                        vo.setDays(daysRecord);
                        vo.setEndDate(DateUtil.date());
                        vo.setStartDate(DateUtil.offsetDay(DateUtil.date(), -6));
                        return vo;
                    }
                    // 7天内一条信息都没有，做空值处理
                    vo.setCount(0L);
                    List<DayRecord> dayRecords = new ArrayList<>();
                    for (int i = 6; i >= 0; i--) {
                        DayRecord dayRecord = new DayRecord();
                        dayRecord.setDate(DateUtil.offsetDay(DateUtil.date(), -i));
                        dayRecord.setCount(0L);
                        dayRecords.add(dayRecord);
                    }
                    vo.setDays(dayRecords);
                    vo.setEndDate(DateUtil.date());
                    vo.setStartDate(DateUtil.offsetDay(DateUtil.date(), -6));
                    return vo;
                });
    }

    private CompletableFuture<SevenDaysCountVO> getTPlusOneSevenDaysCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneSevenDayCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(daysRecord -> {
                    SevenDaysCountVO vo = new SevenDaysCountVO();
                    vo.setDays(daysRecord);
                    vo.setCount(daysRecord.stream().mapToLong(DayRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    vo.setStartDate(DateUtil.offsetDay(dto.getEndDate(), -7));
                    return vo;
                });
    }

    private CompletableFuture<SevenDaysCountVO> getTimeRangeByProvinceSevenDaysCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceSevenDayCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(daysRecord -> {
                    SevenDaysCountVO vo = new SevenDaysCountVO();
                    vo.setDays(daysRecord);
                    vo.setCount(daysRecord.stream().mapToLong(DayRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    vo.setStartDate(DateUtil.offsetDay(dto.getEndDate(), -6));
                    return vo;
                });
    }

    private CompletableFuture<SevenDaysCountVO> getTimeRangeByCitySevenDaysCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCitySevenDayCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(daysRecord -> {
                    SevenDaysCountVO vo = new SevenDaysCountVO();
                    vo.setDays(daysRecord);
                    vo.setCount(daysRecord.stream().mapToLong(DayRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    vo.setStartDate(DateUtil.offsetDay(dto.getEndDate(), -6));
                    return vo;
                });
    }

    private CompletableFuture<XWeeksCountVO> getTPlusOneXWeeksCount(TPlusOneDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneXWeeksCount(dto.getStartDate(), dto.getEndDate(), x, dto.getId()), clickHouseTaskExecutor)
                .thenApply(xWeeksRecords -> {
                    XWeeksCountVO vo = new XWeeksCountVO();
                    vo.setWeekRecords(xWeeksRecords);
                    vo.setCount(xWeeksRecords.stream().mapToLong(WeekRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<XWeeksCountVO> getTimeRangeByProvinceXWeeksCount(TimeAndGeoSearchDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceXWeeksCount(
                                dto.getStartDate(), dto.getEndDate(), x, dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(xWeeksRecords -> {
                    XWeeksCountVO vo = new XWeeksCountVO();
                    vo.setWeekRecords(xWeeksRecords);
                    vo.setCount(xWeeksRecords.stream().mapToLong(WeekRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<XWeeksCountVO> getTimeRangeByCityXWeeksCount(TimeAndGeoSearchDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityXWeeksCount(
                                dto.getStartDate(), dto.getEndDate(), x, dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(xWeeksRecords -> {
                    XWeeksCountVO vo = new XWeeksCountVO();
                    vo.setWeekRecords(xWeeksRecords);
                    vo.setCount(xWeeksRecords.stream().mapToLong(WeekRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryHourCountVO> getTPlusOneHourSegmentCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneHourSegmentCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(hourOfDays -> {
                    EveryHourCountVO vo = new EveryHourCountVO();
                    vo.setHourOfDays(hourOfDays);
                    vo.setCount(hourOfDays.stream().mapToLong(HourOfDay::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<EveryHourCountVO> getTimeRangeByProvinceHourSegmentCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceHourSegmentCount(
                                dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(hourOfDays -> {
                    EveryHourCountVO vo = new EveryHourCountVO();
                    vo.setHourOfDays(hourOfDays);
                    vo.setCount(hourOfDays.stream().mapToLong(HourOfDay::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryHourCountVO> getTimeRangeByCityHourSegmentCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityHourSegmentCount(
                                dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(hourOfDays -> {
                    EveryHourCountVO vo = new EveryHourCountVO();
                    vo.setHourOfDays(hourOfDays);
                    vo.setCount(hourOfDays.stream().mapToLong(HourOfDay::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryProvinceCountVO> getTPlusOneEveryProvinceCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneEveryProvinceCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(provinceRecords -> {
                    EveryProvinceCountVO vo = new EveryProvinceCountVO();
                    vo.setProvinceRecords(provinceRecords);
                    vo.setCount(provinceRecords.stream().mapToLong(ProvinceRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    // todo 不该
    private CompletableFuture<EveryProvinceCountVO> getTimeRangeByProvinceEveryProvinceCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceEveryProvinceCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(provinceRecords -> {
                    EveryProvinceCountVO vo = new EveryProvinceCountVO();
                    vo.setProvinceRecords(provinceRecords);
                    vo.setCount(provinceRecords.stream().mapToLong(ProvinceRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryProvinceCountVO> getTimeRangeByCityEveryProvinceCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityEveryProvinceCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(provinceRecords -> {
                    EveryProvinceCountVO vo = new EveryProvinceCountVO();
                    vo.setProvinceRecords(provinceRecords);
                    vo.setCount(provinceRecords.stream().mapToLong(ProvinceRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<TopXCityCountVO> getTPlusOneTopXCityCount(TPlusOneDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneTopXCityCount(dto.getStartDate(), dto.getEndDate(), x, dto.getId()), clickHouseTaskExecutor)
                .thenApply(topXCityRecords -> {
                    TopXCityCountVO vo = new TopXCityCountVO();
                    vo.setCityRecords(topXCityRecords);
                    vo.setCount(topXCityRecords.stream().mapToLong(CityRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<TopXCityCountVO> getTimeRangeByProvinceTopXCityCount(TimeAndGeoSearchDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceTopXCityCount(dto.getStartDate(), dto.getEndDate(), x, dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(topXCityRecords -> {
                    TopXCityCountVO vo = new TopXCityCountVO();
                    vo.setCityRecords(topXCityRecords);
                    vo.setCount(topXCityRecords.stream().mapToLong(CityRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<TopXCityCountVO> getTimeRangeByCityTopXCityCount(TimeAndGeoSearchDTO dto, Integer x) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityTopXCityCount(dto.getStartDate(), dto.getEndDate(), x, dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(topXCityRecords -> {
                    TopXCityCountVO vo = new TopXCityCountVO();
                    vo.setCityRecords(topXCityRecords);
                    vo.setCount(topXCityRecords.stream().mapToLong(CityRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryBrowserCountVO> getTPlusOneEveryBrowserCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneEveryBrowserCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(browserRecords -> {
                    EveryBrowserCountVO vo = new EveryBrowserCountVO();
                    vo.setBrowserRecords(browserRecords);
                    vo.setCount(browserRecords.stream().mapToLong(BrowserRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<EveryBrowserCountVO> getTimeRangeByProvinceEveryBrowserCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceEveryBrowserCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(browserRecords -> {
                    EveryBrowserCountVO vo = new EveryBrowserCountVO();
                    vo.setBrowserRecords(browserRecords);
                    vo.setCount(browserRecords.stream().mapToLong(BrowserRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryBrowserCountVO> getTimeRangeByCityEveryBrowserCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityEveryBrowserCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(browserRecords -> {
                    EveryBrowserCountVO vo = new EveryBrowserCountVO();
                    vo.setBrowserRecords(browserRecords);
                    vo.setCount(browserRecords.stream().mapToLong(BrowserRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryOSCountVO> getTPlusOneEveryOSCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneEveryOSCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(osRecords -> {
                    EveryOSCountVO vo = new EveryOSCountVO();
                    vo.setOsRecords(osRecords);
                    vo.setCount(osRecords.stream().mapToLong(OSRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<EveryOSCountVO> getTimeRangeByProvinceEveryOSCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceEveryOSCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(osRecords -> {
                    EveryOSCountVO vo = new EveryOSCountVO();
                    vo.setOsRecords(osRecords);
                    vo.setCount(osRecords.stream().mapToLong(OSRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryOSCountVO> getTimeRangeByCityEveryOSCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityEveryOSCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(osRecords -> {
                    EveryOSCountVO vo = new EveryOSCountVO();
                    vo.setOsRecords(osRecords);
                    vo.setCount(osRecords.stream().mapToLong(OSRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<PhoneAndPCCountVO> getTPlusOnePhoneAndPCPercentage(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOnePhoneAndPCCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(phoneAndPCRecord -> {
                    PhoneAndPCCountVO vo = new PhoneAndPCCountVO();
                    vo.setPhoneAndPCRecord(phoneAndPCRecord);
                    vo.setCount(phoneAndPCRecord.getPcCount() + phoneAndPCRecord.getPhoneCount());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<PhoneAndPCCountVO> getTimeRangeByProvincePhoneAndPCPercentage(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvincePhoneAndPCCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(phoneAndPCRecord -> {
                    PhoneAndPCCountVO vo = new PhoneAndPCCountVO();
                    vo.setPhoneAndPCRecord(phoneAndPCRecord);
                    vo.setCount(phoneAndPCRecord.getPcCount() + phoneAndPCRecord.getPhoneCount());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<PhoneAndPCCountVO> getTimeRangeByCityPhoneAndPCPercentage(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityPhoneAndPCCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(phoneAndPCRecord -> {
                    PhoneAndPCCountVO vo = new PhoneAndPCCountVO();
                    vo.setPhoneAndPCRecord(phoneAndPCRecord);
                    vo.setCount(phoneAndPCRecord.getPcCount() + phoneAndPCRecord.getPhoneCount());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryISPCountVO> getTPlusOneEveryISPCount(TPlusOneDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTPlusOneEveryISPCount(dto.getStartDate(), dto.getEndDate(), dto.getId()), clickHouseTaskExecutor)
                .thenApply(ispRecords -> {
                    EveryISPCountVO vo = new EveryISPCountVO();
                    vo.setIspRecords(ispRecords);
                    vo.setCount(ispRecords.stream().mapToLong(ISPRecord::getCount).sum());
                    vo.setEndDate(DateUtil.offsetDay(dto.getEndDate(), -1));
                    return vo;
                });
    }

    private CompletableFuture<EveryISPCountVO> getTimeRangeByProvinceEveryISPCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByProvinceEveryISPCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(ispRecords -> {
                    EveryISPCountVO vo = new EveryISPCountVO();
                    vo.setIspRecords(ispRecords);
                    vo.setCount(ispRecords.stream().mapToLong(ISPRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }

    private CompletableFuture<EveryISPCountVO> getTimeRangeByCityEveryISPCount(TimeAndGeoSearchDTO dto) {
        return CompletableFuture.supplyAsync(() ->
                        analysisMapper.selectTimeRangeByCityEveryISPCount(dto.getStartDate(), dto.getEndDate(), dto.getId(), dto.getGeos()), clickHouseTaskExecutor)
                .thenApply(ispRecords -> {
                    EveryISPCountVO vo = new EveryISPCountVO();
                    vo.setIspRecords(ispRecords);
                    vo.setCount(ispRecords.stream().mapToLong(ISPRecord::getCount).sum());
                    vo.setEndDate(dto.getEndDate());
                    return vo;
                });
    }
}
