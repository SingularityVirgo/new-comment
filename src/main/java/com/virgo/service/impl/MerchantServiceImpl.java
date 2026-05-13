package com.virgo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.po.Merchant;
import com.virgo.domain.po.Shop;
import com.virgo.mapper.MerchantMapper;
import com.virgo.service.IMerchantService;
import com.virgo.service.IShopService;
import com.virgo.utils.SystemConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    private final IShopService shopService;

    @Override
    public Merchant requirePublic(Long id) {
        Merchant m = getById(id);
        if (m == null || m.getStatus() == null || m.getStatus() != 1) {
            throw new BizException("\u5546\u6237\u4e0d\u5b58\u5728\u6216\u5df2\u505c\u7528");
        }
        return m;
    }

    @Override
    public List<Shop> listShopsByMerchant(Long merchantId, Integer current) {
        requirePublic(merchantId);
        Page<Shop> page = shopService
                .lambdaQuery()
                .eq(Shop::getMerchantId, merchantId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return page.getRecords();
    }
}
