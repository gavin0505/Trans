package cc.forim.trans.user.infra.config;

import cc.forim.trans.common.lock.DistributeLockFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁配置
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Configuration
public class DistributeLockConfiguration {

    @Bean
    public DistributeLockFactory distributeLockFactory() {
        return new DistributeLockFactory();
    }
}