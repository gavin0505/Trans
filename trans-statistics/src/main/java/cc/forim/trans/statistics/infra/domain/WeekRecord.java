package cc.forim.trans.statistics.infra.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 周访问记录
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/8 下午11:49
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeekRecord {

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weekStart;

    private Long count;
}
