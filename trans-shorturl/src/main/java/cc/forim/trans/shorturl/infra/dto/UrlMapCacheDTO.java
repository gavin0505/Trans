package cc.forim.trans.shorturl.infra.dto;

import lombok.Data;

import java.util.Date;


/**
 * 内部流转UrlMap -> Cache
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
public class UrlMapCacheDTO {

    private Long id;

    /**
     * 本次任务id
     */
    private String requestId;

    private String shortUrl;

    private String longUrl;

    private String compressionCode;

    private Date expireTime;

    private String bizType;

    private String description;

    private Boolean enable;

    public Long userId;
}