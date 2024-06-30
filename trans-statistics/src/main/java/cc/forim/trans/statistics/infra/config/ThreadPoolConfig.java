package cc.forim.trans.statistics.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Configuration
@RefreshScope
public class ThreadPoolConfig {

    @Value("${trans.pool.ck.core}")
    private Integer corePoolSize;

    @Value("${trans.pool.ck.max-core}")
    private Integer maxPoolSize;

    @Value("${trans.pool.ck.keep-alive-time}")
    private Integer keepAliveTime;

    @Value("${trans.pool.ck.queue-length}")
    private Integer queueSize;

    /**
     * 线程池
     */
    @Bean(name = "clickHouseTaskExecutor")
    public ThreadPoolExecutor clickHouseTaskExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime
                ,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
