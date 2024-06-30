package cc.forim.trans.shorturl.listener;

import cc.forim.trans.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cc.forim.trans.shorturl.infra.constant.CacheKey.ACCESS_CODE_STRING_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CacheKey.EXPIRE_ACCESS_CODE_SET_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CommonConstant.*;

/**
 * 监听Redis中的过期key，用于清除过期短网址
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Component
@Slf4j
public class RedisExpireKeyListener extends KeyExpirationEventMessageListener {

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    public RedisExpireKeyListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
        // 从 __keyevent@*__:expired 订阅过期key，格式：trans:server:access:code:v:cafebabe
        String expiredKey = message.toString();

        if (StringUtils.contains(expiredKey, ACCESS_CODE_STRING_PREFIX.getKey())) {
            log.info("Listened the expired compression code key: {}", expiredKey);

            String[] keySegments = expiredKey.split(COLON);
            String bizType = keySegments[keySegments.length - 2];
            String code = keySegments[keySegments.length - 1];
            // 设置空缓存
            redisUtil.set(expiredKey, EMPTY_CACHE, SEVEN_DAY_SECONDS);
            // 过期key去重
            redisUtil.sSet(EXPIRE_ACCESS_CODE_SET_PREFIX.getKey() + bizType + COLON + SET, code);
        }
    }
}
