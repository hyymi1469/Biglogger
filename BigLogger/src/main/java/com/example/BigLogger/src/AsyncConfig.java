package com.example.BigLogger.src;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);   // 최소 스레드 수
        executor.setMaxPoolSize(8);   // 최대 스레드 수
        executor.setQueueCapacity(10); // 대기 큐 길이
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "dbExecutor")
    public Executor dbExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);     // 동시에 처리할 DB 작업 개수
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500); // 대기 작업 큐 크기
        executor.setThreadNamePrefix("DB-");
        executor.initialize();
        return executor;
    }
}