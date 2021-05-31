package com.atguigu.gmall.search.service.impl;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchPramVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public SearchResponseVo search(SearchPramVo searchPramVo) {
        SearchRequest request = new SearchRequest(new String[]{"goods"},this.dslBuild(searchPramVo));
        try {
            SearchResponse response = this.restHighLevelClient.search(request, RequestOptions.DEFAULT);
            System.out.println(response);
            SearchResponseVo searchResponseVo = this.parseResult(response);
            // 设置页面信息
            searchResponseVo.setPageSize(searchPramVo.getPageSize());
            searchResponseVo.setPageNum(searchPramVo.getPageNum());
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = response.getHits();
        // 解析具体数据
        searchResponseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits == null || hitsHits.length ==0){
            throw new RuntimeException("当前搜索条件没有对应数据！！！！");
        }
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            String json = hitsHit.getSourceAsString();
            try {
                Goods goods = mapper.readValue(json, Goods.class);
                Map<String, HighlightField> map = hitsHit.getHighlightFields();
                HighlightField title = map.get("title");
                if (title != null){
                    goods.setTitle(title.getFragments()[0].toString());
                }
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        // 解析aggragation聚合数据
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedLongTerms brandAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            searchResponseVo.setBrands(buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                // 获取品牌自聚合
                Map<String, Aggregation> subAggMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms)subAggMap.get("brandAgg");
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                // 解析logo结果集
                ParsedStringTerms logoAgg = (ParsedStringTerms)subAggMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)){
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList()));
        }
        // 获取分类的聚合结果集
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)){
            searchResponseVo.setCategories( categoryBuckets.stream().map(Bucket ->{
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) Bucket).getKeyAsNumber().longValue());
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms)((Terms.Bucket) Bucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> categoryNameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(categoryNameAggBuckets)){
                    categoryEntity.setName(categoryNameAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList()));
        }
         // 获取规格参数聚合
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        // 获取嵌套结果集id的聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
            searchResponseVo.setFilters(attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
               // 设置规格参数id
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                // 获取当前桶的子聚合
                Map<String, Aggregation> subAggMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                // 解析
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)subAggMap.get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameBuchets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrNameBuchets)){
                    searchResponseAttrVo.setAttrName(attrNameBuchets.get(0).getKeyAsString());
                }
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)subAggMap.get("attrValueAgg");
                // 获取规格参数子聚合
                List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueAggBuckets)){
                    searchResponseAttrVo.setAttrValues(valueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return searchResponseAttrVo;
                }
            ).collect(Collectors.toList()));
        }

        return searchResponseVo;
    }
    private SearchSourceBuilder dslBuild(SearchPramVo searchPramVo){
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = searchPramVo.getKeyword();
        if (StringUtils.isBlank(keyword)){
            return sourceBuilder;
        }
        //1.检索条件构建
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        //1.2 构建过滤
        //1.2.1 构建品牌过滤
        List<Long> brandId = searchPramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", brandId));
        }
        //1.2.2 构建分类过滤
        List<Long> categoryId = searchPramVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",categoryId));
        }
        //1.2.3 否建价格区间
        Double priceFrom = searchPramVo.getPriceFrom();
        Double priceTo = searchPramVo.getPriceTo();
        if (priceFrom != null || priceTo != null){
            //构建范围查询
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQueryBuilder);
            //判断价格是否为空
            if (priceFrom != null){
                rangeQueryBuilder.gte(priceFrom);
            }
            if (priceTo != null){
                rangeQueryBuilder.lte(priceTo);
            }
        }
        //1.2.4 构建是否有货
        Boolean store = searchPramVo.getStore();
        if (store != null ){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }

        //1.2.5否建规格参数
        List<String> props = searchPramVo.getProps();
        if (!CollectionUtils.isEmpty(props)){
            props.forEach(prop ->{
                //用“：”分割字符串
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs !=null && attrs.length == 2){
                    // 构建bool查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery, ScoreMode.None));
                    String[] attrValues = StringUtils.split(attrs[1],"-");
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attrs[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                }
            });
        }

        //2.排序构建
        Integer sort = searchPramVo.getSort();
        if (sort != null){
            switch (sort){
                case 1:
                    sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 2:
                    sourceBuilder.sort("price",SortOrder.ASC);
                    break;
                case 3:
                    sourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                case 4:
                    sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
                default:
                    sourceBuilder.sort("_score",SortOrder.DESC);
                    break;
            }
        }

        //3.分页
        Integer pageNum = searchPramVo.getPageNum();
        Integer pageSize = searchPramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        //4.构建高亮
        sourceBuilder.highlighter(
                new HighlightBuilder()
                        .field("title")
                        .preTags("<font style='color:red'>")
                        .postTags("</font>")
        );
        //5.构建聚合
        //5.1 品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //5.2 分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //5.3 规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrList")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrList.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrList.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrList.attrValue"))));
        // 结果集过滤
        sourceBuilder.fetchSource(new String[]{"sku_id","title","subTitle","defaultImage","paice"},null);
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
