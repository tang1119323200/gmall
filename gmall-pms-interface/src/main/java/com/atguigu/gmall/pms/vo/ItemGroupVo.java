package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author TangLei
 * @date 2021/5/28
 */
@Data
public class ItemGroupVo {
    private String groupName;
    private List<AttrValueVo> attrs;
}
