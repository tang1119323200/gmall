package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchPramVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

public interface SearchService {
    SearchResponseVo search(SearchPramVo searchPramVo);
}
