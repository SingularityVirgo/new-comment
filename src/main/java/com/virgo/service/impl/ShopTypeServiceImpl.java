package com.virgo.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.virgo.common.exception.BizException;
import com.virgo.entity.ShopType;
import com.virgo.mapper.ShopTypeMapper;
import com.virgo.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.virgo.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
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
            throw new BizException("种类不存在");
        }
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(typeList));
        return typeList;
    }
}
