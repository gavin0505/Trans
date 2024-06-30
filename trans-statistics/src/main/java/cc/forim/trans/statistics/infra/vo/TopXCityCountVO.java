package cc.forim.trans.statistics.infra.vo;

import cc.forim.trans.statistics.infra.domain.CityRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 前X名城市的访问量
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/8 下午11:10
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopXCityCountVO {

    private Long count;

    private List<CityRecord> cityRecords;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
}
