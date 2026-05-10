package com.virgo.service;

import com.virgo.domain.po.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务�?
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherService extends IService<Voucher> {

    List<Voucher> queryVoucherOfShop(Long shopId);

    Voucher getVoucherDetail(Long id);

    void updateVoucher(Voucher voucher);

    void removeVoucher(Long id);

    void addSeckillVoucher(Voucher voucher);
}
