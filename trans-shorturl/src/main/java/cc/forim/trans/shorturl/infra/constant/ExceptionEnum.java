package cc.forim.trans.shorturl.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum ExceptionEnum {

    /**
     * 插入异常
     */
    INSERT_EXCEPTION("500", "插入存储介质时出现异常"),

    /**
     * 查询域名配置信息时出现异常
     */
    SELECT_DOMAIN_CONF_EXCEPTION("500", "查询域名配置信息时出现异常"),

    /**
     * 创建压缩码时异常，因为压缩码已存在
     */
    CREATE_COMPRESSION_CODE_EXCEPTION("500", "创建压缩码时异常，因为压缩码已存在并正在使用: "),

    /**
     * 创建压缩码时异常，因为压缩码已存在
     */
    EXPIRED_TIME_BEYOND_NOW_EXCEPTION("500", "当前时间晚于过期时间，过期时间: "),

    ;

    private final String code;

    private final String description;
}
