package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 手机和PC访问记录
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/9 上午11:13
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneAndPCRecord {

    private Long phoneCount;

    private Long pcCount;
}
