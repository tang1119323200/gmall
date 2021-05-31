package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.fegin.GmallSmsFeginClient;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;

import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
    public SpuDescMapper spuDescMapper;
    @Autowired
    public SpuAttrValueService spuAttrValueService;
    @Autowired
    public SkuMapper skuMapper;
    @Autowired
    public SkuImagesService skuImagesService;
    @Autowired
    public SkuAttrValueService skuAttrValueService;
    @Autowired
    public GmallSmsFeginClient smsFeginClient;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuPageById(long categoryId, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        //categoryId不为 0 时查询
        if (categoryId != 0){
            queryWrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();
        //如果查询调插件不为空时查询
        if (StringUtils.isNoneBlank(key.trim())){
            queryWrapper.and(t -> t.eq("id",key).or().like("name", key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                queryWrapper
        );

        return new PageResultVo(page);

    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        //1.保存spu相关信息
        //1.1 保存pms_spu
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        Long spuId = spu.getId();

        //1.2 保存pms_spu_desc
        List<String> spuImages = spu.getSpuImages();
        if (CollectionUtils.isNotEmpty(spuImages)){

            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages,","));
            this.spuDescMapper.insert(spuDescEntity);

        }
        //1.3 保存pms_spu_attr_values

        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (CollectionUtils.isNotEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities =
                    baseAttrs.stream().map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        BeanUtils.copyProperties(spuAttrValueVo,spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(spuId);
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }


        //2.保存sku相关信息
        // 2.1 保存pms_sku
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuVo -> {
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setCategoryId(spu.getCategoryId());
            List<String> images = skuVo.getImages();
            if (CollectionUtils.isNotEmpty(images)){
                skuVo.setDefaultImage(StringUtils.isBlank(skuVo.getDefaultImage()) ? images.get(0) : skuVo.getDefaultImage());
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            //2.2 保存pms_sku_images
            if (CollectionUtils.isNotEmpty(images)){
                skuImagesService.saveBatch( images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(image,skuVo.getDefaultImage()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));

            }

            //2.3 保存pms_sku_attr_values

            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(skuAttrValueEntity -> {
                skuAttrValueEntity.setSkuId(skuId);
            });
            this.skuAttrValueService.saveBatch(saleAttrs);

            //3.保存营销相关信息
            SkuSalesVo skuSalesVo = new SkuSalesVo();
            BeanUtils.copyProperties(skuVo,skuSalesVo);
            skuSalesVo.setSkuId(skuId);
            smsFeginClient.saveSales(skuSalesVo);

        });
        try {
            this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }




}