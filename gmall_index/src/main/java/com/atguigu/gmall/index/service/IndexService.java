package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.fegin.GmallPmsCline;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {
    @Autowired
    private GmallPmsCline pmsCline;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired()
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates:";
    private static final String LOCK_PREFIX = "index:lock:cates:";
    public List<CategoryEntity> queryCategoriesLv1Index() {
        ResponseVo responseVo = pmsCline.categoryById(0L);
        List<CategoryEntity> categoryEntityList = (List<CategoryEntity>)responseVo.getData();
        return categoryEntityList;
    }
    @GmallCache(prefix = KEY_PREFIX,timeout = 129600,random = 14400,LockPrefix = LOCK_PREFIX)
    public List<CategoryEntity> queryCategoriesLv2Index(Long pid) {
            List<CategoryEntity> categoryEntityList = pmsCline.queryLv2WithSubByPid(pid).getData();
            return categoryEntityList;

    }
}
