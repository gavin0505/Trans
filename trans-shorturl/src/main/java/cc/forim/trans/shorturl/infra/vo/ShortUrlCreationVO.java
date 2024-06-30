package cc.forim.trans.shorturl.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建短链成功视图
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlCreationVO {

    /**
     * 请求创建而传入的id
     */
    private String requestId;

    /**
     * 短链域名
     */
    private String shortUrl;
}