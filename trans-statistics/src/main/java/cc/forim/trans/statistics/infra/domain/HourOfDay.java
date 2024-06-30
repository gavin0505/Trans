package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小时之于日
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/8 下午8:15
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourOfDay {

    private Integer hour;

    private Long count;
}
