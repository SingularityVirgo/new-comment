package com.virgo.controller;

import com.virgo.dto.Result;
import com.virgo.service.IShopTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
