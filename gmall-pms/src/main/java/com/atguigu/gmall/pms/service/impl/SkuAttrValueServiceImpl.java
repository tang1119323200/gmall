package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    AttrMapper attrMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuAttrValueMapper attrValueMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBuCidAndSkuId(Long cid, Long skuId) {
        //1.根据分类id查询出检索类型的规格参数
        List<AttrEntity> attrEntityList = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrEntityList)){
            return null;
        }
        List<Long> attrIds = attrEntityList.stream().map(AttrEntity::getId).collect(Collectors.toList());
        //2.根据skuid和attrid查询销售类型规格参数

        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId).in("attr_id",attrIds));
    }
    @Override
    public List<SaleAttrValueVo> querySalesAttrValuesBySpuId(Long spuId) {
        // 查询sku集合
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuIds));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        ArrayList<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId,AttrValueEntities) ->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(AttrValueEntities.get(0).toString());
            saleAttrValueVo.setAttrValue(AttrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String querySaleAttrValuesMappingSkuIdBySpuId(Long spuId) {
        // 获取spuId集合
        List<SkuEntity> skuEntitys = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntitys)){
            return null;
        }
        List<Long> skuIds = skuEntitys.stream().map(SkuEntity::getId).collect(Collectors.toList());

        //查询映射
        Map<String,Long> map = this.attrValueMapper.querySaleAttrValuesMappingSkuIdBySpuId(skuIds);
        return null;
    }

}