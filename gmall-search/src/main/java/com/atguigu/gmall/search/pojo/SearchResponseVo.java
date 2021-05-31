package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {
    // 品牌过滤列表
    private List<BrandEntity> brands;

    // 分类的过滤列表
    private List<CategoryEntity> categories;

    //规格参数的过滤列表
    private  List<SearchResponseAttrVo> filters;

    // 总记录数
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    // 商品信息
    private List<Goods> goodsList;
}
