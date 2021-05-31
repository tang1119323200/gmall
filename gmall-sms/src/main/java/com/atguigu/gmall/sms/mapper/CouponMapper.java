package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author tangtang
 * @email tangtang@atguigu.com
 * @date 2021-05-15 13:58:56
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponEntity> {
	
}
