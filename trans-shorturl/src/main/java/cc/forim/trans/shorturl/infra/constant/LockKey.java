package cc.forim.trans.shorturl.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分布式锁KEY
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@RequiredArgsConstructor
@Getter
public enum LockKey {

    /**
     * 创建短链映射场景
     */
    CREATE_URL_MAP("trans:short_url:map:create", "创建URL映射", 0L, 10000L),

    /**
     * 编辑短链映射场景
     */
    EDIT_URL_MAP("trans:short_url:map:edit", "修改URL映射", 0L, 10000L),

    /**
     * 删除过期短链映射场景（定时任务）
     */
    DELETE_EXPIRED_URL_MAP("trans:short_url:map:delete", "删除过期URL映射", 0L, 9000L),

    /**
     * 删除过期短链映射时补偿的场景（定时任务）
     */
    DELETE_EXPIRED_URL_MAP_COMPENSATION("trans:short_url:map:delete:compensation", "删除过期短链映射时补偿", 0L, 9000L),
    ;

    private final String code;
    private final String desc;
    private final long waitTime;
    private final long releaseTime;
}