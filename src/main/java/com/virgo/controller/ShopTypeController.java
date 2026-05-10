package com.virgo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.virgo.domain.dto.shoptype.ShopTypeWriteCommand;
import com.virgo.domain.po.ShopType;
import com.virgo.web.api.Result;
import com.virgo.service.IShopTypeService;
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
@RequestMapping("/shop-type")
@RequiredArgsConstructor
public class ShopTypeController {

    private final IShopTypeService typeService;

    @GetMapping("list")
    public Result<?> queryTypeList() {
        return Result.ok(WebModels.toShopTypeVos(typeService.queryTypeList()));
    }

    @PostMapping
    public Result<?> save(@RequestBody ShopTypeWriteCommand command) {
        ShopType shopType = BeanUtil.copyProperties(command, ShopType.class);
        return Result.ok(typeService.saveShopType(shopType));
    }

    @PutMapping
    public Result<?> update(@RequestBody ShopTypeWriteCommand command) {
        ShopType shopType = BeanUtil.copyProperties(command, ShopType.class);
        typeService.updateShopType(shopType);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable("id") Long id) {
        typeService.removeShopType(id);
        return Result.ok();
    }
}
