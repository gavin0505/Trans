package cc.forim.trans.shorturl.infra.vo;

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
public class ShortUrlUpdateVO {

    private Long id;

    /**
     * 请求创建而传入的id
     */
    private String requestId;

    private String longUrl;

    private String description;


}
