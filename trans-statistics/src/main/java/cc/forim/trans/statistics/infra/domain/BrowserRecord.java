package cc.forim.trans.statistics.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 浏览器记录
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/9 上午10:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrowserRecord {

    private String browser;

    private Long count;
}
