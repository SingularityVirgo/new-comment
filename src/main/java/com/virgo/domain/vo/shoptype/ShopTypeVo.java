package com.virgo.domain.vo.shoptype;

import lombok.Data;

/** 店铺类型（接口响应，不含内部审计时间） */
@Data
public class ShopTypeVo {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}
