package com.virgo.controller;

import com.virgo.service.IVoucherService;
import com.virgo.web.api.Result;
import com.virgo.web.assembly.WebModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    @GetMapping("/list/{shopId}")
    public Result<?> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return Result.ok(WebModels.toVoucherVos(voucherService.queryVoucherOfShop(shopId)));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") Long id) {
        return Result.ok(WebModels.toVoucherVo(voucherService.getVoucherDetail(id)));
    }
}
