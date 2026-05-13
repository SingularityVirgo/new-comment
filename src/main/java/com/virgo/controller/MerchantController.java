package com.virgo.controller;

import com.virgo.domain.po.Merchant;
import com.virgo.domain.po.Shop;
import com.virgo.service.IMerchantService;
import com.virgo.web.api.Result;
import com.virgo.web.assembly.WebModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端：商户公开信息与旗下店铺列表（与用户个人页等路由独立）。
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final IMerchantService merchantService;

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") Long id) {
        Merchant m = merchantService.requirePublic(id);
        return Result.ok(WebModels.toMerchantPublicVo(m));
    }

    @GetMapping("/{id}/shops")
    public Result<?> shops(
            @PathVariable("id") Long id,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        List<Shop> list = merchantService.listShopsByMerchant(id, current);
        return Result.ok(WebModels.toShopListItemVos(list));
    }
}
