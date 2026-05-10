package com.virgo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.virgo.domain.dto.voucher.VoucherWriteCommand;
import com.virgo.domain.po.Voucher;
import com.virgo.web.api.Result;
import com.virgo.service.IVoucherService;
import com.virgo.web.assembly.WebModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    @PostMapping
    public Result<?> addVoucher(@RequestBody VoucherWriteCommand command) {
        Voucher voucher = BeanUtil.copyProperties(command, Voucher.class);
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    @PostMapping("seckill")
    public Result<?> addSeckillVoucher(@RequestBody VoucherWriteCommand command) {
        Voucher voucher = BeanUtil.copyProperties(command, Voucher.class);
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    public Result<?> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return Result.ok(WebModels.toVoucherVos(voucherService.queryVoucherOfShop(shopId)));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") Long id) {
        return Result.ok(WebModels.toVoucherVo(voucherService.getVoucherDetail(id)));
    }

    @PutMapping
    public Result<?> update(@RequestBody VoucherWriteCommand command) {
        Voucher voucher = BeanUtil.copyProperties(command, Voucher.class);
        voucherService.updateVoucher(voucher);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable("id") Long id) {
        voucherService.removeVoucher(id);
        return Result.ok();
    }
}
