package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.entity.Shop;
import com.virgo.service.IShopService;
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

    @GetMapping("/{id}")
    public Result<?> queryShopById(@PathVariable("id") Long id) {
        return Result.ok(shopService.queryById(id));
    }

    @PostMapping
    public Result<?> saveShop(@RequestBody Shop shop) {
        shopService.save(shop);
        return Result.ok(shop.getId());
    }

    @PutMapping
    public Result<?> updateShop(@RequestBody Shop shop) {
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
        return Result.ok(shopService.queryShopByType(typeId, current, x, y));
    }

    @GetMapping("/of/name")
    public Result<?> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(shopService.queryShopByName(name, current));
    }
}
