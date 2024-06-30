package cc.forim.trans.shorturl.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 压缩码状态
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum CompressionCodeStatus {

    /**
     * 可用
     */
    AVAILABLE(1, "可用"),

    /**
     * 已经使用
     */
    USED(2, "已经使用"),

    /**
     * 已失效
     */
    INVALID(3, "已失效"),

    ;

    private final Integer value;
    private final String status;
}