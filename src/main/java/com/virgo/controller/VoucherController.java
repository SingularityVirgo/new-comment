package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.entity.Voucher;
import com.virgo.service.IVoucherService;
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
    public Result<?> addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    @PostMapping("seckill")
    public Result<?> addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    public Result<?> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return Result.ok(voucherService.queryVoucherOfShop(shopId));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") Long id) {
        return Result.ok(voucherService.getVoucherDetail(id));
    }

    @PutMapping
    public Result<?> update(@RequestBody Voucher voucher) {
        voucherService.updateVoucher(voucher);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable("id") Long id) {
        voucherService.removeVoucher(id);
        return Result.ok();
    }
}
