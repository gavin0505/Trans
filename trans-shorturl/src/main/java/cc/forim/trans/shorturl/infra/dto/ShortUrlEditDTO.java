package cc.forim.trans.shorturl.infra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 编辑短网址的传输数据
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortUrlEditDTO {

    private Long id;

    /**
     * 本次任务id
     */
    private String requestId;

    private String shortUrl;

    private String longUrl;

    private String compressionCode;

    private String domain;

    private String bizType;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    private String urlStatus;

    private Long userId;

    private String username;

    private String creator;

    private String editor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTime;

    private String deleted;
}
