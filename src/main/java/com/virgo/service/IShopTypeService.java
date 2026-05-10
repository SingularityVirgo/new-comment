package com.virgo.service;

import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.virgo.dto.Result;
import com.virgo.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {


    Result queryTypeList();
}
