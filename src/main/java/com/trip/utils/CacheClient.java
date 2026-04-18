package com.trip.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import com.trip.constants.RedisCommonConstants;
import com.trip.constants.RedisLockConstants;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = createCacheRebuildExecutor();

    private static ExecutorService createCacheRebuildExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "cache-rebuild-" + count.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };

        return new ThreadPoolExecutor(
                10,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R,ID> List<R> queryListWithPassThrough(
            String keyPrefix,
            ID id,
            Class<R> elementType,
            Supplier<List<R>> dbFallback,
            Long time, TimeUnit unit) {

        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.parseArray(json).toList(elementType);
        }

        if (json != null) {
            return null;
        }

        List<R> list = dbFallback.get();
        if (list == null || list.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", RedisCommonConstants.CACHE_NULL_TTL.toMinutes(), TimeUnit.MINUTES);
            return null;
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list), time, unit);

        return list;
    }

    // 查询后写回空值防止缓存穿透
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }

        R r = dbFallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisCommonConstants.CACHE_NULL_TTL.toMinutes(), TimeUnit.MINUTES);
            return null;
        }
        this.set(key, r, time, unit);
        return r;
    }

    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((String) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if(expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }
        String lockKey = RedisLockConstants.LOCK_SCENIC_PREFIX + id;
        boolean isLock = tryLock(lockKey);
        if (isLock){
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R newR = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });
        }
        return r;
    }

    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String scenicJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(scenicJson)) {
            return JSONUtil.toBean(scenicJson, type);
        }
        if (scenicJson != null) {
            return null;
        }

        String lockKey = RedisLockConstants.LOCK_SCENIC_PREFIX + id;
        R r;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            r = dbFallback.apply(id);
            if (r == null) {
                stringRedisTemplate.opsForValue().set(key, "", RedisCommonConstants.CACHE_NULL_TTL.toMinutes(), TimeUnit.MINUTES);
                return null;
            }
            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            unlock(lockKey);
        }
        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisLockConstants.LOCK_SCENIC_TTL.toSeconds(), TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
