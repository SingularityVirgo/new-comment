package com.virgo.domain.dto.shop;

import lombok.Data;

/** 创建/更新店铺（写请求体，与表字段对齐） */
@Data
public class ShopWriteCommand {
    private Long id;
    private String name;
    private Long typeId;
    private Long merchantId;
    private String images;
    private String area;
    private String address;
    private Double x;
    private Double y;
    private Long avgPrice;
    private Integer sold;
    private Integer comments;
    private Integer score;
    private String openHours;
}
