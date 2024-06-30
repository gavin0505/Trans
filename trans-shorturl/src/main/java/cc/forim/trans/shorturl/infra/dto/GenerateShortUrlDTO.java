package cc.forim.trans.shorturl.infra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 请求生成短链的传输数据（不指定短URL）
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class GenerateShortUrlDTO {

    /**
     * 本次任务id
     */
    private String requestId;

    /**
     * 短链域名
     */
    private String domain;

    /**
     * 指定的压缩码
     */
    private String specialCompressionCode;

    /**
     * 长Url
     */
    private String longUrl;

    /**
     * 短链类别标识
     */
    private String bizType;

    /**
     * 失效日期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidateDate;

    /**
     * 描述
     */
    private String description;

    /**
     * 用户id
     */
    @JsonIgnore
    private Long userId;
}