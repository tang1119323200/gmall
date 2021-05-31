package com.atguigu.gmall.search.pojo;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.Data;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.security.PrivateKey;
import java.util.List;
import java.util.function.Function;

@Data
public class SearchResponseAttrVo {

    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
