package cc.forim.trans.user.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 邮件发送内容枚举
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Getter
@ToString
@AllArgsConstructor
public enum EmailContentEnum {

    /**
     * 注册验证码
     */
    VERIFY_CODE_TO_REGISTER("0001", "注册验证码: {}", "您正在进行Armagin短链接平台的【注册】操作。本次登录的验证码为:【{}】, {}分钟内有效。");

    private final String code;

    /**
     * 邮件标题
     */
    private final String title;

    private final String content;
}