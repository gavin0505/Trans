package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作系统记录
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/9 上午11:05
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSRecord {

    private String os;

    private Long count;
}
