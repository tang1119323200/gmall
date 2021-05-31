package com.atguigu.gmall.index.config;

import com.atguigu.gmall.index.fegin.GmallPmsCline;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author TangLei
 * @date 2021/5/28
 */
@Configuration
public class BloomFilterConfig {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private GmallPmsCline pmsCline;

    private static final String KEY_PREFIX = "index:cates:";
    @Bean
    public RBloomFilter rBloomFilter(){
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("index:bloom");
        bloomFilter.tryInit(500,0.03);
        List<CategoryEntity> categoryEntityList = this.pmsCline.categoryById(0l).getData();
        if (CollectionUtils.isNotEmpty(categoryEntityList)){
            categoryEntityList.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + categoryEntity.getId());
            });
        }
        return bloomFilter;
    }
}
