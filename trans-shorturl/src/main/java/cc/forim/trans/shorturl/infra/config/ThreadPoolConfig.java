package cc.forim.trans.shorturl.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

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
@EnableAsync
public class ThreadPoolConfig {

    /**
     * Redis过期事件监听 线程池
     */
    @Bean(name = "scheduledTaskExecutor")
    public ThreadPoolExecutor scheduledTaskExecutor() {
        return new ThreadPoolExecutor(
                4,
                6,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

}
