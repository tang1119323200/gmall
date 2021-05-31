package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author tangtang
 * @email tangtang@atguigu.com
 * @date 2021-05-13 18:26:33
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}
