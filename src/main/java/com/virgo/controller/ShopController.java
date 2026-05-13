package com.virgo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.virgo.common.exception.BizException;
import com.virgo.domain.dto.shop.ShopWriteCommand;
import com.virgo.domain.po.Shop;
import com.virgo.domain.vo.shop.ShopDetailVo;
import com.virgo.web.api.Result;
import com.virgo.service.IMerchantService;
import com.virgo.service.IShopService;
import com.virgo.web.assembly.WebModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final IShopService shopService;
    private final IMerchantService merchantService;

    @GetMapping("/{id}")
    public Result<?> queryShopById(@PathVariable("id") Long id) {
        Shop shop = shopService.queryById(id);
        ShopDetailVo vo = WebModels.toShopDetailVo(shop);
        if (shop.getMerchantId() != null) {
            try {
                vo.setMerchant(WebModels.toMerchantPublicVo(merchantService.requirePublic(shop.getMerchantId())));
            } catch (BizException ex) {
                // 商户停用：仍返回店铺，不附带商户展示块
            }
        }
        return Result.ok(vo);
    }

    @PostMapping
    public Result<?> saveShop(@RequestBody ShopWriteCommand command) {
        Shop shop = BeanUtil.copyProperties(command, Shop.class);
        shopService.save(shop);
        return Result.ok(shop.getId());
    }

    @PutMapping
    public Result<?> updateShop(@RequestBody ShopWriteCommand command) {
        Shop shop = BeanUtil.copyProperties(command, Shop.class);
        shopService.updateShop(shop);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> removeShop(@PathVariable("id") Long id) {
        shopService.removeShop(id);
        return Result.ok();
    }

    @GetMapping("/of/type")
    public Result<?> queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y) {
        return Result.ok(WebModels.toShopListItemVos(shopService.queryShopByType(typeId, current, x, y)));
    }

    @GetMapping("/of/name")
    public Result<?> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(WebModels.toShopListItemVos(shopService.queryShopByName(name, current)));
    }
}
