package com.virgo.domain.dto.voucher;

import lombok.Data;

import java.time.LocalDateTime;

/** 创建/更新优惠券（含秒杀扩展字段，写请求体） */
@Data
public class VoucherWriteCommand {
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
}
