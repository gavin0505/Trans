package cc.forim.trans.shorturl.infra.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 续签短链接视图
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRenewalVO {

    private Long id;

    /**
     * 请求创建而传入的id
     */
    private String requestId;

    private String shortUrl;

    /**
     * 续签后，最终的过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date renewalTime;
}
