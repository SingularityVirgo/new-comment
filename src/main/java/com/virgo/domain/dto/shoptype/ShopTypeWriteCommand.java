package com.virgo.domain.dto.shoptype;

import lombok.Data;

/** 创建/更新店铺类型 */
@Data
public class ShopTypeWriteCommand {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}
