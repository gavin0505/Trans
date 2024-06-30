package cc.forim.trans.statistics.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通过时间和地理标签查到的多维度数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllByGeoAndTimeSpanVO {

    private TotalCountVO totalCountVO;

    private XWeeksCountVO xWeeksCountVO;

    private SevenDaysCountVO sevenDaysCountVO;

    private EveryHourCountVO everyHourCountVO;

    private EveryProvinceCountVO everyProvinceCountVO;

    private TopXCityCountVO topXCityCountVO;

    private EveryBrowserCountVO everyBrowserCountVO;

    private EveryOSCountVO everyOSCountVO;

    private PhoneAndPCCountVO phoneAndPCCountVO;

    private EveryISPCountVO everyISPCountVO;
}
