package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.fegin.GmallPmsCline;
import com.atguigu.gmall.search.fegin.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {
    @Autowired
    private GmallPmsCline gmallPmsCline;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("SEARCH_INSERT_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return ;
        }
        // 根据spuId查询
        ResponseVo<SpuEntity> responseVo = this.gmallPmsCline.querySpuById(spuId);
        SpuEntity spuEntity = responseVo.getData();
        if (spuEntity == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return ;
        }

        ResponseVo<List<SkuEntity>> listResponseVo = this.gmallPmsCline.querySkuById(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)){
            //查询品牌
            ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsCline.queryBrandById(spuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            //查询分类
            ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsCline.queryCategoryById(spuEntity.getCategoryId());
            CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                //设置时间
                goods.setCreateTime(spuEntity.getCreateTime());
                //设置销量
                ResponseVo<List<WareSkuEntity>> wmsResponseVo = this.gmallWmsClient.queryWareSkuByPage(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntityList = wmsResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntityList)){
                    goods.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked()>0));
                    goods.setSales(wareSkuEntityList.stream().map(WareSkuEntity::getSales).reduce((a,b) -> a+b).get());
                }
                //设置品牌分类参数
                if (brandEntity != null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                // 参数
                if(categoryEntity != null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                List<SearchAttrValue> searchAttrValues = new ArrayList();
                //设置索引类型规格参数
                //销售类型参数
                ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = this.gmallPmsCline.querySearchAttrValueBuCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(skuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                //检索类型参数
                ResponseVo<List<SpuAttrValueEntity>> spuAttrResponseVo = gmallPmsCline.querySearchAttrValuesByCidAndSpuId(skuEntity.getCategoryId(), skuEntity.getSpuId());
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue =  new SearchAttrValue();
                        BeanUtils.copyProperties(spuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                goods.setSearchAttrList(searchAttrValues);
                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            //重试
            if (message.getMessageProperties().getRedelivered()) {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

        }
    }
}
