package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author TangLei
 * @date 2021/5/30
 */
public class ThreadPollConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(100,500,60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }
}
