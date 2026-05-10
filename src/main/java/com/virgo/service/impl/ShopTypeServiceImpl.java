package com.virgo.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.virgo.common.exception.BizException;
import com.virgo.domain.po.ShopType;
import com.virgo.mapper.ShopTypeMapper;
import com.virgo.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.virgo.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * \u5e97\u94fa\u7c7b\u578b\u670d\u52a1\u5b9e\u73b0\u3002
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        if (StrUtil.isNotBlank(shopTypeJson)) {
            return JSONUtil.toList(shopTypeJson, ShopType.class);
        }
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        if (typeList == null || typeList.isEmpty()) {
            throw new BizException("\u6682\u65e0\u5e97\u94fa\u7c7b\u578b\u6570\u636e");
        }
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(typeList));
        return typeList;
    }

    @Override
    public Long saveShopType(ShopType shopType) {
        if (!save(shopType)) {
            throw new BizException("\u4fdd\u5b58\u5e97\u94fa\u7c7b\u578b\u5931\u8d25");
        }
        stringRedisTemplate.delete(CACHE_SHOP_TYPE_KEY);
        return shopType.getId();
    }

    @Override
    public void updateShopType(ShopType shopType) {
        if (shopType.getId() == null) {
            throw new BizException("\u5e97\u94fa\u7c7b\u578bid\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!updateById(shopType)) {
            throw new BizException("\u66f4\u65b0\u5e97\u94fa\u7c7b\u578b\u5931\u8d25");
        }
        stringRedisTemplate.delete(CACHE_SHOP_TYPE_KEY);
    }

    @Override
    public void removeShopType(Long id) {
        if (getById(id) == null) {
            throw new BizException("\u5e97\u94fa\u7c7b\u578b\u4e0d\u5b58\u5728");
        }
        if (!removeById(id)) {
            throw new BizException("\u5220\u9664\u5e97\u94fa\u7c7b\u578b\u5931\u8d25");
        }
        stringRedisTemplate.delete(CACHE_SHOP_TYPE_KEY);
    }
}
