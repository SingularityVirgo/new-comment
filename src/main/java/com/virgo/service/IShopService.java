package com.virgo.service;

import com.virgo.domain.po.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务�?
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Shop queryById(Long id);

    void updateShop(Shop shop);

    List<Shop> queryShopByType(Integer typeId, Integer current, Double x, Double y);

    List<Shop> queryShopByName(String name, Integer current);

    void removeShop(Long id);
}
