package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.checkerframework.checker.units.qual.A;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author TangLei
 * @date 2021/5/28
 */
@Aspect
@Component
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        // 获取注解对象
        Method method = signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        // 获取目标返回值类型
        Class<?> returnType = method.getReturnType();
        // 获取注解属性
        String prefix = annotation.prefix();
        String lockPrefix = annotation.LockPrefix();
        int random = new Random().nextInt(annotation.random());
        int timeout = annotation.timeout();

        String args = StringUtils.join(joinPoint.getArgs(),",");
        String key = prefix + args;

        if (!bloomFilter.contains(key)){
            return null;
        }
        // 查询缓存
        String json = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isBlank(json)){
            return JSON.parseObject(json,returnType);
        }
        // 加分布式锁
        Object result;
        RLock fairLock = redissonClient.getFairLock(lockPrefix + args);
        fairLock.lock();
        try {
            // 再次判断缓存中是否存在数据
            // 如果存在就返回
            String json2 = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isBlank(json2)){
                return JSON.parseObject(json2,returnType);
            }
            // 执行目标方法
            result = joinPoint.proceed(joinPoint.getArgs());
            if (result != null){
                // 返回值放入缓存
                this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result),
                        timeout + random,
                        TimeUnit.MINUTES);
            }
            return result;
        } finally {
            fairLock.unlock();
        }


    }
}
