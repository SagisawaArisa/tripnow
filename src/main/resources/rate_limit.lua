local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expireTime = tonumber(ARGV[2])

local current = redis.call('INCR', key)

if current == 1 then
    redis.call('EXPIRE', key, expireTime)
end

if current > limit then
    return 1 -- Limit Exceeded
end

return 0 -- Allowed
