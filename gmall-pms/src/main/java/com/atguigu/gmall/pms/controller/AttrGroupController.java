package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author tangtang
 * @email tangtang@atguigu.com
 * @date 2021-05-13 18:26:33
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("spuId")Long skuId
    ){
        List<ItemGroupVo> itemGroupVoList = this.attrGroupService.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(cid,skuId,spuId);
        return ResponseVo.ok(itemGroupVoList);
    }

    @GetMapping("category/{cid}")
    @ApiOperation("查询三级分类的分组")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupsById(@PathVariable("cid") long cid){
        List<AttrGroupEntity> list = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        return ResponseVo.ok(list);
    }
    @GetMapping("withattrs/{catId}")
    @ApiOperation("查询三级分类的分组")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupsByCatId(@PathVariable("catId") long catId){
        List<AttrGroupEntity> list = attrGroupService.queryAttrGroupsByCatId(catId);
        return ResponseVo.ok(list);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
