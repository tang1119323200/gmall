package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author tangtang
 * @email tangtang@atguigu.com
 * @date 2021-05-13 18:26:33
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryAttrGroupsByCatId(long catId);

    List<ItemGroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long skuId, Long spuId);
}

