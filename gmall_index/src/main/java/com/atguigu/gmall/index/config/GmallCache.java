package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存过期时间
     * @return
     */
    int timeout() default 30;

    /**
     * 随机时间
     * @return
     */
    int random() default 10;

    /**
     * 分布式锁前缀
     * @return
     */
    String LockPrefix() default "lock:";
}
