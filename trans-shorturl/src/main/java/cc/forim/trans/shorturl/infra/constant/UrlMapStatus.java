package cc.forim.trans.shorturl.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UrlMap可用状态
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum UrlMapStatus {

    /**
     * 可用状态
     */
    AVAILABLE(1, "正常"),

    /**
     * 失效状态
     */
    INVALID(2, "已失效"),

    ;

    private final Integer value;

    private final String status;
}