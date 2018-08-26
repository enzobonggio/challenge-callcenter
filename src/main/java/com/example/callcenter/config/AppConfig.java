package com.example.callcenter.config;

import com.example.callcenter.exception.OutOfEmployeeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executor;

@Configuration
public class AppConfig {

    @Bean("callcenter-executor")
    public Executor executorCallcenter(
            @Value("${executor.callcenter.poolcore:5}") int poolCore,
            @Value("${executor.callcenter.poolqueue:0}") int poolQueue,
            @Value("${executor.callcenter.poolmax:10}") int poolMax,
            @Value("${executor.callcenter.prefxi:callcenter}") String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolCore);
        executor.setMaxPoolSize(poolMax);
        executor.setQueueCapacity(poolQueue);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }

    @Bean("callcenter-scheduler")
    public Scheduler schedulerCallcenter(@Qualifier("callcenter-executor") Executor executor) {
        return Schedulers.fromExecutor(executor);
    }
}
