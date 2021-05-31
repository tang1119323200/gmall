package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author tangtang
 * @email tangtang@atguigu.com
 * @date 2021-05-13 18:26:33
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {


    List<CategoryEntity> querySubCategoriesById(Long pid);
}
