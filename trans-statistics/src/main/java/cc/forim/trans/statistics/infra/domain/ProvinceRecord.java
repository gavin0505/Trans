package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 省份记录
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceRecord {

    private String province;

    private Long count;
}
