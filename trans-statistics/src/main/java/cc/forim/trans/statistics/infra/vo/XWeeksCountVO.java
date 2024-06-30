package cc.forim.trans.statistics.infra.vo;

import cc.forim.trans.statistics.infra.domain.WeekRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 近x周的访问数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XWeeksCountVO {

    private Long count;

    private List<WeekRecord> weekRecords;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
}
