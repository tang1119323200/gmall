package com.atguigu.gmall.search.controller;
import com.atguigu.gmall.search.pojo.SearchPramVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Slf4j
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping("search")
    public String search(SearchPramVo searchPramVo, Model model){
        SearchResponseVo responseVo = this.searchService.search(searchPramVo);
        model.addAttribute("response",responseVo);
        model.addAttribute("searchParam",searchPramVo);
        return "search";

    }
}
