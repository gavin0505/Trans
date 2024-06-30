package cc.forim.trans.statistics.infra.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * T+1数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPlusOneVO {

    private TotalCountVO totalCountVO;

    @JsonProperty("xWeeksCountVO")
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
