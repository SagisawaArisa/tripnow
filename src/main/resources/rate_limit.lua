local key = KEYS[1]
local limit = tonumber(ARGV[1])       -- 窗口内的令牌总数
local timeWindow = tonumber(ARGV[2])  -- 时间窗口
local now = tonumber(ARGV[3])         -- 当前时间戳

-- 令牌补充速率
local rate = limit / (timeWindow * 1000)

-- 获取上次补充的令牌数和时间
local lastTokens = redis.call('HGET', key, 'tokens')
local lastTime = redis.call('HGET', key, 'lastTime')

if lastTokens == false then
    -- 初始令牌 = 上限
    redis.call('HSET', key, 'tokens', limit)
    redis.call('HSET', key, 'lastTime', now)
    redis.call('HSET', key, 'tokens', limit - 1)
    redis.call('EXPIRE', key, timeWindow)
    return 0 -- 允许
end

lastTokens = tonumber(lastTokens)
lastTime = tonumber(lastTime)

local elapsedTime = math.max(0, now - lastTime)
-- 补充令牌
local newTokens = math.min(limit, lastTokens + elapsedTime * rate)

-- 检查是否有足够的令牌
if newTokens >= 1 then
    -- 消费令牌
    newTokens = newTokens - 1
    redis.call('HSET', key, 'tokens', newTokens)
    redis.call('HSET', key, 'lastTime', now)
    redis.call('EXPIRE', key, timeWindow)
    return 0 -- 允许
else
    -- 令牌不足
    redis.call('HSET', key, 'tokens', newTokens)
    redis.call('HSET', key, 'lastTime', now)
    redis.call('EXPIRE', key, timeWindow)
    return 1 -- 拒绝
end
