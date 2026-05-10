-- 1.参数列表
-- KEYS[1]: 库存key
-- KEYS[2]: 订单key
-- KEYS[3]: Stream名称
-- ARGV[1]: 用户id
-- ARGV[2]: 订单id


-- 2.数据key (直接从KEYS获取，无需拼接)
local stockKey = KEYS[1]
local orderKey = KEYS[2]
local streamKey = KEYS[3]
local userId = ARGV[1]
local orderId = ARGV[2]

-- 3.脚本业务
-- 3.1.判断库存是否充足 get stockKey
if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.2.库存不足，返回1
    return 1
end
-- 3.2.判断用户是否下单 SISMEMBER orderKey userId
if(redis.call('sismember', orderKey, userId) == 1) then
    -- 3.3.存在，说明是重复下单，返回2
    return 2
end
-- 3.4.扣库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)
-- 3.5.下单（保存用户）sadd orderKey userId
redis.call('sadd', orderKey, userId)
-- 3.6.发送消息到队列中， XADD streamKey * key value ...
redis.call('xadd', streamKey, '*', 'userId', userId, 'orderId', orderId)
return 0