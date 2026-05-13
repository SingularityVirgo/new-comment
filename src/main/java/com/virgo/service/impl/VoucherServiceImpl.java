package com.virgo.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.po.SeckillVoucher;
import com.virgo.domain.po.Voucher;
import com.virgo.mapper.VoucherMapper;
import com.virgo.service.ISeckillVoucherService;
import com.virgo.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.virgo.utils.RedisConstants.VOUCHER_STOCK_KEY;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        return getBaseMapper().queryVoucherOfShop(shopId);
    }

    @Override
    public Voucher getVoucherDetail(Long id) {
        Voucher v = getBaseMapper().queryVoucherDetailById(id);
        if (v == null) {
            throw new BizException("\u4f18\u60e0\u5238\u4e0d\u5b58\u5728");
        }
        return v;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVoucher(Voucher voucher) {
        if (voucher.getId() == null) {
            throw new BizException("\u4f18\u60e0\u5238id\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (getById(voucher.getId()) == null) {
            throw new BizException("\u4f18\u60e0\u5238\u4e0d\u5b58\u5728");
        }
        LambdaUpdateWrapper<Voucher> w = new LambdaUpdateWrapper<Voucher>().eq(Voucher::getId, voucher.getId());
        if (voucher.getShopId() != null) {
            w.set(Voucher::getShopId, voucher.getShopId());
        }
        if (voucher.getTitle() != null) {
            w.set(Voucher::getTitle, voucher.getTitle());
        }
        if (voucher.getSubTitle() != null) {
            w.set(Voucher::getSubTitle, voucher.getSubTitle());
        }
        if (voucher.getRules() != null) {
            w.set(Voucher::getRules, voucher.getRules());
        }
        if (voucher.getPayValue() != null) {
            w.set(Voucher::getPayValue, voucher.getPayValue());
        }
        if (voucher.getActualValue() != null) {
            w.set(Voucher::getActualValue, voucher.getActualValue());
        }
        if (voucher.getType() != null) {
            w.set(Voucher::getType, voucher.getType());
        }
        if (voucher.getStatus() != null) {
            w.set(Voucher::getStatus, voucher.getStatus());
        }
        boolean mainChanged = voucher.getShopId() != null
                || voucher.getTitle() != null
                || voucher.getSubTitle() != null
                || voucher.getRules() != null
                || voucher.getPayValue() != null
                || voucher.getActualValue() != null
                || voucher.getType() != null
                || voucher.getStatus() != null;
        if (mainChanged && !update(w)) {
            throw new BizException("\u66f4\u65b0\u4f18\u60e0\u5238\u5931\u8d25");
        }
        if (seckillVoucherService.getById(voucher.getId()) != null) {
            if (voucher.getStock() != null || voucher.getBeginTime() != null || voucher.getEndTime() != null) {
                SeckillVoucher patch = new SeckillVoucher();
                patch.setVoucherId(voucher.getId());
                if (voucher.getStock() != null) {
                    patch.setStock(voucher.getStock());
                }
                if (voucher.getBeginTime() != null) {
                    patch.setBeginTime(voucher.getBeginTime());
                }
                if (voucher.getEndTime() != null) {
                    patch.setEndTime(voucher.getEndTime());
                }
                seckillVoucherService.updateById(patch);
            }
            if (voucher.getStock() != null) {
        stringRedisTemplate.opsForValue().set(VOUCHER_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeVoucher(Long id) {
        if (getById(id) == null) {
            throw new BizException("\u4f18\u60e0\u5238\u4e0d\u5b58\u5728");
        }
        if (seckillVoucherService.getById(id) != null) {
            seckillVoucherService.removeById(id);
        }
        stringRedisTemplate.delete(VOUCHER_STOCK_KEY + id);
        if (!removeById(id)) {
            throw new BizException("\u5220\u9664\u4f18\u60e0\u5238\u5931\u8d25");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(Voucher voucher) {
        save(voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        stringRedisTemplate.opsForValue().set(VOUCHER_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
