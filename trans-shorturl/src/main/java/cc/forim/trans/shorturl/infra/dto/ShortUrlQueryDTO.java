package cc.forim.trans.shorturl.infra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 短链接查询的传输数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortUrlQueryDTO {

    private Long id;

    /**
     * 本次任务id
     */
    private String requestId;

    private String protocol;

    private String domain;

    private String bizType;

    private String description;

    private String shortUrl;

    private String longUrl;

    private String compressionCode;

    private Integer urlStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 分页时的每页条数
     */
    private Integer pageCount;

    /**
     * 页数
     */
    private Integer page;

    private Long userId;
}
