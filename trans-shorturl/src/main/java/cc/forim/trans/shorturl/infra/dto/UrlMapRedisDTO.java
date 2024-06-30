package cc.forim.trans.shorturl.infra.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 最终缓存到Redis的UrlMap
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@Builder
public class UrlMapRedisDTO {

    private Long id;

    private Long userId;

    private String longUrl;

    private String compressionCode;

    private Date expireTime;
}
