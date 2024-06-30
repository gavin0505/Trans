package cc.forim.trans.shorturl.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建短网址时需要用到的域名配置信息
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainConfSelectionVO {

    private String protocol;

    private String domainValue;

    private String bizType;
}
