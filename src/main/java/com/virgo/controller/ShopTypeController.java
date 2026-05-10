package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.entity.ShopType;
import com.virgo.service.IShopTypeService;
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
@RequestMapping("/shop-type")
@RequiredArgsConstructor
public class ShopTypeController {

    private final IShopTypeService typeService;

    @GetMapping("list")
    public Result<?> queryTypeList() {
        return Result.ok(typeService.queryTypeList());
    }

    @PostMapping
    public Result<?> save(@RequestBody ShopType shopType) {
        return Result.ok(typeService.saveShopType(shopType));
    }

    @PutMapping
    public Result<?> update(@RequestBody ShopType shopType) {
        typeService.updateShopType(shopType);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable("id") Long id) {
        typeService.removeShopType(id);
        return Result.ok();
    }
}
