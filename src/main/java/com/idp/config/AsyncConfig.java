package com.idp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
public class AsyncConfig {

    @Value("${audit.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${audit.executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${audit.executor.queue-capacity:500}")
    private int queueCapacity;

    /**
     * 감사 로그 전용 비동기 스레드풀.
     * AuditEventListener의 @Async("auditExecutor")에서 참조.
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
