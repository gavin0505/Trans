package cc.forim.trans.shorturl.infra.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 返回结果状态枚举
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Getter
@ToString
@AllArgsConstructor
public enum ResultStatusEnum {

    /**
     * 创建短链接失败
     */
    CREATE_SHORT_URL_FAILED("500", "创建短链接失败"),

    /**
     * 创建短链接成功
     */
    CREATE_SHORT_URL_SUCCESS("200", "创建短链接成功");

    private final String code;

    private final String description;
}