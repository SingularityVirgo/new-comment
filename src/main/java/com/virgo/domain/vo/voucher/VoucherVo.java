package com.virgo.domain.vo.voucher;

import lombok.Data;

import java.time.LocalDateTime;

/** 优惠券（接口响应） */
@Data
public class VoucherVo {
    private Long id;
    private Long shopId;
    private String title;
    private String subTitle;
    private String rules;
    private Long payValue;
    private Long actualValue;
    private Integer type;
    private Integer status;
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
