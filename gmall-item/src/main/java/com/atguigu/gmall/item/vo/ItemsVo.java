package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author TangLei
 * @date 2021/5/28
 */
@Data
public class ItemsVo{

    // 一二三级分类
    private List<CategoryEntity> categoryEntities;
    // 品牌相关信息
    private Long brandId;
    private String bandName;
    // spu相关信息
    private Long spuId;
    private String spuName;

    // sku相关信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImages;
    private Integer weight;

    // 优惠信息
    private List<ItemSaleVo> sales; //营销信息
    // 图片列表
    private List<SkuImagesEntity> images;

    // 看是否有货
    private Boolean store;

    // 销售属性列表
    private List<SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性
    private Map<Long,String> saleAttr;

    //销售属性个skuId关系
    private String skuJsons;

    // 商品描述信息
    private List<String> spuImages;
    // 商品描述信息
    private List<?> groups;


}
