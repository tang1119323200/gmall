package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.fegin.GmallPmsCline;
import com.atguigu.gmall.item.fegin.GmallSmsFeginClient;
import com.atguigu.gmall.item.fegin.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemsVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author TangLei
 * @date 2021/5/30
 */
@Service
public class ItemService {
    @Autowired
    private GmallPmsCline pmsCline;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsFeginClient smsFeginClient;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private TemplateEngine templateEngine;
    public ItemsVo loadItem(long skuId) {
        ItemsVo itemsVo = new ItemsVo();
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            //1 根据skuId查询sku
            SkuEntity skuEntity = pmsCline.querySkuById(skuId).getData();
            if (skuEntity != null) {
                itemsVo.setSkuId(skuEntity.getId());
                itemsVo.setTitle(skuEntity.getTitle());
                itemsVo.setSubTitle(skuEntity.getSubtitle());
                itemsVo.setPrice(skuEntity.getPrice());
                itemsVo.setWeight(skuEntity.getWeight());
                itemsVo.setDefaultImages(skuEntity.getDefaultImage());
            }
            return skuEntity;
        }, executor);
        // 根据三级分类的id查询一二三级分类
        CompletableFuture<Void> catesFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<CategoryEntity> categoryEntities = this.pmsCline.queryLv123CatesByCid3(skuEntity.getCategoryId()).getData();
            itemsVo.setCategoryEntities(categoryEntities);
        }, executor);

        // 查询品牌
        CompletableFuture<Void> bandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            BrandEntity brandEntity = this.pmsCline.queryBrandById(skuEntity.getBrandId()).getData();
            if (brandEntity != null) {
                itemsVo.setBrandId(brandEntity.getId());
                itemsVo.setBandName(brandEntity.getName());
            }

        }, executor);
        // 查询spu
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            SpuEntity spuEntity = this.pmsCline.querySpuById(skuEntity.getSpuId()).getData();
            if (spuEntity != null) {
                itemsVo.setSpuId(spuEntity.getId());
                itemsVo.setSpuName(spuEntity.getName());
            }

        }, executor);

        // 查询营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            List<ItemSaleVo> itemSaleVo = this.smsFeginClient.querySalesBySkuId(skuId).getData();
            if (CollectionUtils.isNotEmpty(itemSaleVo)) {
                itemsVo.setSales(itemSaleVo);

            }

        }, executor);

        // 查询sku图片列表
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = this.pmsCline.queryImagesBySkuId(skuId).getData();
            itemsVo.setImages(skuImagesEntities);

        }, executor);

        // 查询库存列表
        CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareSkuByPage(skuId).getData();
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                itemsVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));

            }

        }, executor);


        // 查询spu下sku销售属性
        CompletableFuture<Void> skuSaleFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<SaleAttrValueVo> saleAttrValueVos = this.pmsCline.querySalesAttrValuesBySpuId(skuEntity.getSpuId()).getData();
            itemsVo.setSaleAttrs(saleAttrValueVos);

        }, executor);

        // 根据sku查询sku销售属性
        CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
            List<SkuAttrValueEntity> skuAttrValueEntities = this.pmsCline.querySaleAttrValuesBySkuId(skuId).getData();
            if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                itemsVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
            }
        }, executor);


        // 根据spuId查询spu下所有销售属性组合与skuId的映射关系
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            String json = this.pmsCline.querySaleAttrValuesMappingSkuIdBySpuId(skuEntity.getSpuId()).getData();
            itemsVo.setSkuJsons(json);

        }, executor);

        // 根据spuId查询描述信息
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            SpuDescEntity spuDescEntity = this.pmsCline.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null) {
                itemsVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }

        }, executor);

        // 根据id，spuId，skuId查询规格参数分组及分组下规格参数和值
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<ItemGroupVo> itemGroupVos = this.pmsCline.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
            itemsVo.setGroups(itemGroupVos);

        }, executor);

        CompletableFuture.allOf(catesFuture,groupFuture,descFuture,mappingFuture,skuAttrFuture,
                skuSaleFuture,wareFuture,salesFuture,spuFuture,bandFuture,imagesFuture).join();
        // 生成静态页面
        executor.execute(()->{
            this.generateHtml(itemsVo);
        });

        return itemsVo;
    }

    private void generateHtml(ItemsVo itemsVo){
        Context context = new Context();
        context.setVariable("itemVo",itemsVo);
        try (PrintWriter printWriter = new PrintWriter(new File("C:\\com.guigu\\html\\" +itemsVo.getSkuId() +".html"))){
            this.templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
