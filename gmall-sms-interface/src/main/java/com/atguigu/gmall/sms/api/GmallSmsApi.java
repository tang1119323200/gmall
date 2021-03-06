package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/saveSales")
    public ResponseVo<Object> saveSales(@RequestBody SkuSalesVo skuSalesVo);
    @GetMapping("sms/skubounds/sales/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable("skuId")Long skuId);
}
