package cc.forim.trans.shorturl.infra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 续签短网址的传输数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenewalShortUrlDTO {

    /**
     * 本次任务id
     */
    private String requestId;

    /**
     * 短URL
     */
    private String shortUrl;

    /**
     * 长URL
     */
    private String longUrl;

    /**
     * 短链域名
     */
    private String domain;

    /**
     * 压缩码
     */
    private String compressionCode;

    /**
     * 短链类别标识
     */
    private String bizType;

    /**
     * 续签后的时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidateDate;

    /**
     * 用户id
     */
    @JsonIgnore
    private Long userId;
}
