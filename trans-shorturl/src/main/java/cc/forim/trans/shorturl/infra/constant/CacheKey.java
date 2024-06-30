package cc.forim.trans.shorturl.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 缓存KEY
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum CacheKey {

    /**
     * 客户端黑名单列表
     */
    BLOCK_IP_SET("trans:server:block:ip:set", "禁用的客户端IP", -1L),

    /**
     * 可访问短链域名白名单列表
     */
    ACCESS_DOMAIN_SET("trans:server:access:domain:set", "启用的短链域名", -1L),

    /**
     * 可访问的压缩码映射前缀
     */
    ACCESS_CODE_STRING_PREFIX("trans:server:access:code:", "可访问的压缩码映射", -1L),

    /**
     * 压缩码缓存池
     */
    COMPRESSION_CODE_POOL_SET("trans:short_url:sequence_pool:set", "压缩码缓存池", -1L),

    /**
     * 域名配置信息缓存
     */
    DOMAIN_CONF_HASH("trans:short_url:domain_conf:hash", "域名配置信息缓存", -1L),

    /**
     * 可访问的压缩码映射的过期时间前缀
     */
    EXPIRE_ACCESS_CODE_ZSET_PREFIX("trans:server:expire:access:code:", "可访问的压缩码映射时间", -1L),

    /**
     * 可访问的压缩码映射的过期时间前缀
     */
    EXPIRE_ACCESS_CODE_SET_PREFIX("trans:server:expire:access:code:", "过期的压缩码前缀", -1L),

    /**
     * 压缩码id前缀
     */
    ACCESS_CODE_STRING_ID_PREFIX("trans:server:code:url_map:id:", "压缩码id前缀", -1L),

    ;

    private final String key;
    private final String description;
    private final long expireSeconds;
}