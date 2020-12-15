package com.changgou.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Nullable
    @Override
    public Executor getAsyncExecutor(){
        //定义线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数(默认是8个)
        taskExecutor.setCorePoolSize(20);
        //线程池最大线程数(如果超过次数，则拒绝执行)
        taskExecutor.setMaxPoolSize(40);
        //线程队列最大线程数
        taskExecutor.setQueueCapacity(10);
        //初始化
        taskExecutor.initialize();

        return taskExecutor;
    }
}
