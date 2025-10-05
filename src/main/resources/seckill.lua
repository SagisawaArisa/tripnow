local voucherId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 获取库存
local stock = redis.call('get', stockKey)

-- 判断库存是否存在
if (stock == false) then
    return 1 -- 视为库存不足
end

-- 判断库存是否充足
if(tonumber(stock) <= 0) then
    return 1
end
-- 是否下单
if(redis.call('sismember', orderKey, userId) == 1) then
    -- 重复下单
    return 2
end
-- 扣库存
redis.call('incrby', stockKey, -1)
-- 下单
redis.call('sadd', orderKey, userId)
return 0