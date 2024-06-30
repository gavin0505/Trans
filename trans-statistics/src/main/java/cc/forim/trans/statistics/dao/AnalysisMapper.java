package cc.forim.trans.statistics.dao;

import cc.forim.trans.statistics.infra.annotation.ClickHouseMapper;
import cc.forim.trans.statistics.infra.domain.*;
import cc.forim.trans.statistics.infra.entity.Analysis;
import cc.forim.trans.statistics.infra.vo.TimeIntervalAndFrequencyVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Gavin Zhang
 * @version V1.0
 */
@ClickHouseMapper
public interface AnalysisMapper extends BaseMapper<Analysis> {

    TimeIntervalAndFrequencyVO selectAllTimeIntervalAndFrequencyInHour();

    TimeIntervalAndFrequencyVO selectTimeIntervalAndFrequencyInHourByUrlMapId(@Param("urlMapId") Long urlMapId,
                                                                              @Param("userId") Long userId);

    TimeIntervalAndFrequencyVO selectTimeIntervalAndFrequencyInHourByUserId(@Param("userId") Long userId);

    TimeIntervalAndFrequencyVO selectAllTimeIntervalAndFrequencyInWeek();

    TimeIntervalAndFrequencyVO selectTimeIntervalAndFrequencyInWeekByUrlMapId(@Param("urlMapId") Long urlMapId,
                                                                              @Param("userId") Long userId);

    TimeIntervalAndFrequencyVO selectTimeIntervalAndFrequencyInWeekByUserId(@Param("userId") Long userId);

    //    @Options(useCache = false)
    Long selectTPlusOneTotalCount(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate,
                                  @Param("urlMapId") Long urlMapId);

    Long selectTimeRangeByProvinceTotalCount(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate,
                                             @Param("urlMapId") Long urlMapId,
                                             @Param("provinces") List<String> provinces);

    Long selectTimeRangeByCityTotalCount(@Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate,
                                         @Param("urlMapId") Long urlMapId,
                                         @Param("citys") List<String> citys);

    List<DayRecord> selectTPlusOneSevenDayCount(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("urlMapId") Long urlMapId);

    List<DayRecord> getTSevenDaysCountByUserId(@Param("userId")Long userId);

    List<DayRecord> selectTimeRangeByProvinceSevenDayCount(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("urlMapId") Long urlMapId,
                                                           @Param("provinces") List<String> provinces);

    List<DayRecord> selectTimeRangeByCitySevenDayCount(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("urlMapId") Long urlMapId,
                                                       @Param("citys") List<String> citys);

    List<WeekRecord> selectTPlusOneXWeeksCount(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("x") Integer x,
                                               @Param("urlMapId") Long urlMapId);

    List<WeekRecord> selectTimeRangeByProvinceXWeeksCount(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("x") Integer x,
                                                          @Param("urlMapId") Long urlMapId,
                                                          @Param("provinces") List<String> provinces);

    List<WeekRecord> selectTimeRangeByCityXWeeksCount(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("x") Integer x,
                                                      @Param("urlMapId") Long urlMapId,
                                                      @Param("citys") List<String> citys);

    List<HourOfDay> selectTPlusOneHourSegmentCount(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("urlMapId") Long urlMapId);

    List<HourOfDay> selectTimeRangeByProvinceHourSegmentCount(@Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate,
                                                              @Param("urlMapId") Long urlMapId,
                                                              @Param("provinces") List<String> provinces);

    List<HourOfDay> selectTimeRangeByCityHourSegmentCount(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("urlMapId") Long urlMapId,
                                                          @Param("citys") List<String> citys);


    List<ProvinceRecord> selectTPlusOneEveryProvinceCount(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("urlMapId") Long urlMapId);

    List<ProvinceRecord> selectTimeRangeByProvinceEveryProvinceCount(@Param("startDate") Date startDate,
                                                                     @Param("endDate") Date endDate,
                                                                     @Param("urlMapId") Long urlMapId,
                                                                     @Param("provinces") List<String> provinces);

    List<ProvinceRecord> selectTimeRangeByCityEveryProvinceCount(@Param("startDate") Date startDate,
                                                                 @Param("endDate") Date endDate,
                                                                 @Param("urlMapId") Long urlMapId,
                                                                 @Param("citys") List<String> citys);

    List<CityRecord> selectTPlusOneTopXCityCount(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("x") Integer x,
                                                 @Param("urlMapId") Long urlMapId);


    List<CityRecord> selectTimeRangeByProvinceTopXCityCount(@Param("startDate") Date startDate,
                                                            @Param("endDate") Date endDate,
                                                            @Param("x") Integer x,
                                                            @Param("urlMapId") Long urlMapId,
                                                            @Param("provinces") List<String> provinces);

    List<CityRecord> selectTimeRangeByCityTopXCityCount(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("x") Integer x,
                                                        @Param("urlMapId") Long urlMapId,
                                                        @Param("citys") List<String> citys);

    List<BrowserRecord> selectTPlusOneEveryBrowserCount(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("urlMapId") Long urlMapId);

    List<BrowserRecord> selectTimeRangeByProvinceEveryBrowserCount(@Param("startDate") Date startDate,
                                                                   @Param("endDate") Date endDate,
                                                                   @Param("urlMapId") Long urlMapId,
                                                                   @Param("provinces") List<String> provinces);

    List<BrowserRecord> selectTimeRangeByCityEveryBrowserCount(@Param("startDate") Date startDate,
                                                               @Param("endDate") Date endDate,
                                                               @Param("urlMapId") Long urlMapId,
                                                               @Param("citys") List<String> citys);


    List<OSRecord> selectTPlusOneEveryOSCount(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("urlMapId") Long urlMapId);

    List<OSRecord> selectTimeRangeByProvinceEveryOSCount(@Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate,
                                                         @Param("urlMapId") Long urlMapId,
                                                         @Param("provinces") List<String> provinces);


    List<OSRecord> selectTimeRangeByCityEveryOSCount(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("urlMapId") Long urlMapId,
                                                     @Param("citys") List<String> citys);


    PhoneAndPCRecord selectTPlusOnePhoneAndPCCount(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("urlMapId") Long urlMapId);

    PhoneAndPCRecord selectTimeRangeByProvincePhoneAndPCCount(@Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate,
                                                              @Param("urlMapId") Long urlMapId,
                                                              @Param("provinces") List<String> provinces);

    PhoneAndPCRecord selectTimeRangeByCityPhoneAndPCCount(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("urlMapId") Long urlMapId,
                                                          @Param("citys") List<String> citys);

    List<ISPRecord> selectTPlusOneEveryISPCount(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("urlMapId") Long urlMapId);

    List<ISPRecord> selectTimeRangeByProvinceEveryISPCount(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("urlMapId") Long urlMapId,
                                                @Param("provinces") List<String> provinces);

    List<ISPRecord> selectTimeRangeByCityEveryISPCount(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("urlMapId") Long urlMapId,
                                                @Param("citys") List<String> citys);

    Long getUrlMapVisitCountByUserId(@Param("userId")Long userId);


    Long getTodayUrlMapVisitCountById(@Param("userId")Long userId);

    Long getTodayUrlMapVisitIpCountById(@Param("userId")Long userId);
}
