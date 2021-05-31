package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchPramVo {
    //关键字
    private String keyword;
    //品牌过滤条件
    private List<Long> brandId;
    //分类过滤条件
    private List<Long> categoryId;
    // 规格参数过滤条件
    private List<String> props;
    // 排序条件：默认-得分排序1：价格降序，2：价格升序，3：销量降序，：销量升序
    // 价格区间
    private Double priceFrom;
    private Double priceTo;

    // 仅显示有货
    private  Boolean store;

    //排序
    private Integer sort;

    private Integer pageNum = 1;
    private final Integer pageSize = 20;
}
