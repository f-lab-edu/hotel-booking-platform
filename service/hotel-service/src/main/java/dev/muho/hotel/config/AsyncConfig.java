package dev.muho.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 기본 스레드 수 (Core Pool Size)
        executor.setCorePoolSize(40);
        // 2. 최대 스레드 수 (Max Pool Size)
        executor.setMaxPoolSize(120);
        // 3. 대기 큐 크기 (Queue Capacity)
        executor.setQueueCapacity(200);
        // 4. 스레드 이름 접두사 (Thread Name Prefix)
        executor.setThreadNamePrefix("async-task-");

        executor.initialize();

        return executor;
    }
}
