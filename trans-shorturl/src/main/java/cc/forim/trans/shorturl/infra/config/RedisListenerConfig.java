package cc.forim.trans.shorturl.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis相关的配置类
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Configuration
public class RedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        // Redis 消息订阅(监听)者容器
        RedisMessageListenerContainer messageListenerContainer = new RedisMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // messageListenerContainer.addMessageListener(new ProductUpdateListener(), new PatternTopic("*.product.update"));
        return messageListenerContainer;
    }
}
