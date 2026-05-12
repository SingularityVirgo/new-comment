package com.virgo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virgo.common.exception.BizException;
import com.virgo.domain.po.VoucherOrder;
import com.virgo.mapper.VoucherOrderMapper;
import com.virgo.service.ISeckillVoucherService;
import com.virgo.service.IVoucherOrderService;
import com.virgo.utils.RedisIdWorker;
import com.virgo.security.CurrentUserAccessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * \u4f18\u60e0\u5238\u8ba2\u5355\u670d\u52a1\u5b9e\u73b0\u3002
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (stringRedisTemplate.getConnectionFactory() == null) {
                        log.warn("Redis connection factory is null, exiting pending list handler");
                        break;
                    }
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    createVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("LettuceConnectionFactory was destroyed")) {
                        log.warn("Redis connection was destroyed, exiting pending list handler");
                        break;
                    }
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
                    );
                    if (list == null || list.isEmpty()) {
                        break;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    createVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("\u5904\u7406\u8ba2\u5355\u5f02\u5e38", e);
                }
            }
        }
    }

    private void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("\u83b7\u53d6\u9501\u5931\u8d25\uff0c\u65e0\u6cd5\u521b\u5efa\u8ba2\u5355");
            return;
        }

        try {
            int count = Math.toIntExact(query().eq("user_id", userId).eq("voucher_id", voucherId).count());
            if (count > 0) {
                log.error("\u4e0d\u80fd\u91cd\u590d\u4e0b\u5355");
                return;
            }

            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0)
                    .update();
            if (!success) {
                log.error("\u5e93\u5b58\u4e0d\u8db3\uff01");
                return;
            }

            save(voucherOrder);
        } finally {
            redisLock.unlock();
        }
    }

    @Override
    public Long seckillVoucher(Long voucherId) {
        Long userId = CurrentUserAccessor.require().getId();
        long orderId = redisIdWorker.nextId("order");

        List<String> keys = new ArrayList<>();
        keys.add("voucher:stock:" + voucherId);
        keys.add("voucher:order:" + voucherId);
        keys.add("stream:seckill");

        List<String> args = new ArrayList<>();
        args.add(userId.toString());
        args.add(String.valueOf(orderId));

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                keys,
                args.toArray()
        );
        int r = result.intValue();
        if (r != 0) {
            throw new BizException(r == 1 ? "\u5e93\u5b58\u4e0d\u8db3" : "\u4e0d\u80fd\u91cd\u590d\u4e0b\u5355");
        }
        return orderId;
    }
}
