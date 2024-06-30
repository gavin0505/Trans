package cc.forim.trans.user.infra.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 锁枚举
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum LockKey {

    /**
     * 创建新账号
     */
    REGISTER_NEW_ACCOUNT("armagin:user:account:create", "创建新账号", 0L, 10000L),

    /**
     * 创建新邮箱
     */
    REGISTER_NEW_EMAIL("armagin:user:email:create", "创建新邮箱", 0L, 10000L),


    ;

    private final String code;
    private final String desc;
    private final long waitTime;
    private final long releaseTime;
}