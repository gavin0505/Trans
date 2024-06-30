package cc.forim.trans.statistics.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeIntervalAndFrequencyVO {

    private String timeInterval;

    private Long frequency;
}
