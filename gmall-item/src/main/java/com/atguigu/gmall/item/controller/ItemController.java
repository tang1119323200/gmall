package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author TangLei
 * @date 2021/5/30
 */
@Controller
public class ItemController {
    @Autowired
    private ItemService itemService;
    @GetMapping("{skuId}.html")
    @ResponseBody
    public ResponseVo<ItemsVo> loadItem(@PathVariable("skuId")long skuId){
        ItemsVo itemsVo = this.itemService.loadItem(skuId);
        return ResponseVo.ok(itemsVo);
    }
}
