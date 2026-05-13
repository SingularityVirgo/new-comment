package com.virgo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.virgo.domain.po.SeckillVoucher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

    @Select("SELECT sv.voucher_id AS voucherId, sv.stock, sv.begin_time AS beginTime, sv.end_time AS endTime, "
            + "sv.create_time AS createTime, sv.update_time AS updateTime FROM tb_seckill_voucher sv "
            + "INNER JOIN tb_voucher v ON v.id = sv.voucher_id WHERE v.status = 1 AND sv.stock > 0 "
            + "AND sv.end_time > #{now} AND sv.begin_time <= #{preheatUntil}")
    List<SeckillVoucher> selectForPreheat(
            @Param("now") LocalDateTime now, @Param("preheatUntil") LocalDateTime preheatUntil);
}
