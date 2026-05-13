package com.virgo.domain.vo.shop;

import com.virgo.domain.vo.merchant.MerchantPublicVo;
import lombok.Data;

import java.time.LocalDateTime;

/** 店铺详情（接口响应） */
@Data
public class ShopDetailVo {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Double distance;
    /** 有归属商户时返回 */
    private MerchantPublicVo merchant;
}
