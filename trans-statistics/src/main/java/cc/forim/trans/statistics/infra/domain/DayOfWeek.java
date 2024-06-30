package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日之于星期
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/8 下午7:25
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayOfWeek {

    private Integer day;

    private Long count;
}
