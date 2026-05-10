package com.virgo.service;

import com.virgo.domain.po.ShopType;
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
public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> queryTypeList();

    Long saveShopType(ShopType shopType);

    void updateShopType(ShopType shopType);

    void removeShopType(Long id);
}
