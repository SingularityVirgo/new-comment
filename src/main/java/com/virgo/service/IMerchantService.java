package com.virgo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virgo.domain.po.Merchant;
import com.virgo.domain.po.Shop;

import java.util.List;

public interface IMerchantService extends IService<Merchant> {

    Merchant requirePublic(Long id);

    List<Shop> listShopsByMerchant(Long merchantId, Integer current);
}
