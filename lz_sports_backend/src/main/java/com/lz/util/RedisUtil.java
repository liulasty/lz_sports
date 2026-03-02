package com.lz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作工具类
 * 封装 Redis 常用操作，包含字符串、哈希、列表、集合、有序集合、过期时间等核心功能
 */
@Component
public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ============================ 通用操作 ============================

    /**
     * 删除指定key
     * @param key 可以传一个或多个key
     * @return 是否删除成功
     */
    public boolean del(String... key) {
        if (key == null || key.length == 0) {
            log.warn("Redis del: key参数为空");
            return false;
        }
        try {
            if (key.length == 1) {
                boolean result = redisTemplate.delete(key[0]);
                log.info("Redis del: {} -> {}", key[0], result);
                return result;
            } else {
                Long deleteCount = redisTemplate.delete(Arrays.asList(key));
                log.info("Redis del: {} -> 删除数量: {}", Arrays.toString(key), deleteCount);
                return deleteCount > 0;
            }
        } catch (Exception e) {
            log.error("Redis del 异常, keys: {}", Arrays.toString(key), e);
            return false;
        }
    }

    /**
     * 设置key的过期时间
     * @param key 键
     * @param expire 过期时间（默认单位：秒）
     * @return 是否设置成功
     */
    public boolean expire(String key, long expire) {
        return expire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * 设置key的过期时间（指定时间单位）
     * @param key 键
     * @param expire 过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long expire, TimeUnit timeUnit) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis expire: key参数为空");
            return false;
        }
        if (expire <= 0) {
            log.warn("Redis expire: 过期时间必须大于0, key: {}, expire: {}", key, expire);
            return false;
        }
        try {
            boolean result = redisTemplate.expire(key, expire, timeUnit);
            log.info("Redis expire: {} -> {} {}", key, expire, timeUnit.name());
            return result;
        } catch (Exception e) {
            log.error("Redis expire 异常, key: {}, expire: {}", key, expire, e);
            return false;
        }
    }

    /**
     * 获取key的过期时间
     * @param key 键
     * @return 过期时间（秒），返回-1表示永久有效，返回-2表示key不存在
     */
    public long getExpire(String key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 获取key的过期时间（指定时间单位）
     * @param key 键
     * @param timeUnit 时间单位
     * @return 过期时间，返回-1表示永久有效，返回-2表示key不存在
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis getExpire: key参数为空");
            return -2;
        }
        try {
            Long expire = redisTemplate.getExpire(key, timeUnit);
            log.info("Redis getExpire: {} -> {} {}", key, expire, timeUnit.name());
            return expire == null ? -2 : expire;
        } catch (Exception e) {
            log.error("Redis getExpire 异常, key: {}", key, e);
            return -2;
        }
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis hasKey: key参数为空");
            return false;
        }
        try {
            Boolean exists = redisTemplate.hasKey(key);
            boolean result = exists != null && exists;
            log.info("Redis hasKey: {} -> {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis hasKey 异常, key: {}", key, e);
            return false;
        }
    }

    // ============================ String 操作 ============================

    /**
     * 普通缓存放入（无过期时间）
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean set(String key, Object value) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis set: key参数为空");
            return false;
        }
        if (value == null) {
            log.warn("Redis set: value参数为空, key: {}", key);
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
            log.info("Redis set: {} -> {}", key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set 异常, key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 普通缓存放入（指定过期时间，单位：秒）
     * @param key 键
     * @param value 值
     * @param expire 过期时间（秒），expire > 0 才会设置过期时间
     * @return 是否成功
     */
    public boolean set(String key, Object value, long expire) {
        return set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 普通缓存放入（指定过期时间和时间单位）
     * @param key 键
     * @param value 值
     * @param expire 过期时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public boolean set(String key, Object value, long expire, TimeUnit timeUnit) {
        if (!set(key, value)) {
            return false;
        }
        if (expire > 0) {
            return expire(key, expire, timeUnit);
        }
        return true;
    }

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis get: key参数为空");
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.info("Redis get: {} -> {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis get 异常, key: {}", key, e);
            return null;
        }
    }

    /**
     * 递增（原子操作）
     * @param key 键
     * @param delta 递增步长（必须大于0）
     * @return 递增后的值
     */
    public long incr(String key, long delta) {
        if (delta <= 0) {
            log.warn("Redis incr: 递增步长必须大于0, key: {}, delta: {}", key, delta);
            throw new IllegalArgumentException("递增步长必须大于0");
        }
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.info("Redis incr: {} -> 步长{}, 结果{}", key, delta, result);
            return result == null ? 0 : result;
        } catch (Exception e) {
            log.error("Redis incr 异常, key: {}, delta: {}", key, delta, e);
            return 0;
        }
    }

    /**
     * 递减（原子操作）
     * @param key 键
     * @param delta 递减步长（必须大于0）
     * @return 递减后的值
     */
    public long decr(String key, long delta) {
        if (delta <= 0) {
            log.warn("Redis decr: 递减步长必须大于0, key: {}, delta: {}", key, delta);
            throw new IllegalArgumentException("递减步长必须大于0");
        }
        try {
            Long result = redisTemplate.opsForValue().increment(key, -delta);
            log.info("Redis decr: {} -> 步长{}, 结果{}", key, delta, result);
            return result == null ? 0 : result;
        } catch (Exception e) {
            log.error("Redis decr 异常, key: {}, delta: {}", key, delta, e);
            return 0;
        }
    }

    // ============================ Hash 操作 ============================

    /**
     * Hash缓存放入
     * @param key 键
     * @param hashKey hash键
     * @param value 值
     * @return 是否成功
     */
    public boolean hset(String key, String hashKey, Object value) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) {
            log.warn("Redis hset: key或hashKey参数为空");
            return false;
        }
        if (value == null) {
            log.warn("Redis hset: value参数为空, key: {}, hashKey: {}", key, hashKey);
            return false;
        }
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            log.info("Redis hset: {} -> {}:{}", key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hset 异常, key: {}, hashKey: {}, value: {}", key, hashKey, value, e);
            return false;
        }
    }

    /**
     * Hash缓存获取
     * @param key 键
     * @param hashKey hash键
     * @return 值
     */
    public Object hget(String key, String hashKey) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) {
            log.warn("Redis hget: key或hashKey参数为空");
            return null;
        }
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            log.info("Redis hget: {} -> {}:{}", key, hashKey, value);
            return value;
        } catch (Exception e) {
            log.error("Redis hget 异常, key: {}, hashKey: {}", key, hashKey, e);
            return null;
        }
    }

    /**
     * 获取指定key下的所有hash键值对
     * @param key 键
     * @return hash键值对集合
     */
    public Map<Object, Object> hgetAll(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis hgetAll: key参数为空");
            return Collections.emptyMap();
        }
        try {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            log.info("Redis hgetAll: {} -> 大小{}", key, map.size());
            return map;
        } catch (Exception e) {
            log.error("Redis hgetAll 异常, key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 删除指定key下的hash键
     * @param key 键
     * @param hashKeys hash键（可传多个）
     * @return 删除的数量
     */
    public long hdel(String key, Object... hashKeys) {
        if (key == null || key.isEmpty() || hashKeys == null || hashKeys.length == 0) {
            log.warn("Redis hdel: key或hashKeys参数为空");
            return 0;
        }
        try {
            Long deleteCount = redisTemplate.opsForHash().delete(key, hashKeys);
            log.info("Redis hdel: {} -> {} -> 删除数量{}", key, Arrays.toString(hashKeys), deleteCount);
            return deleteCount == null ? 0 : deleteCount;
        } catch (Exception e) {
            log.error("Redis hdel 异常, key: {}, hashKeys: {}", key, Arrays.toString(hashKeys), e);
            return 0;
        }
    }

    /**
     * 判断指定key下的hash键是否存在
     * @param key 键
     * @param hashKey hash键
     * @return 是否存在
     */
    public boolean hhasKey(String key, String hashKey) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) {
            log.warn("Redis hhasKey: key或hashKey参数为空");
            return false;
        }
        try {
            Boolean exists = redisTemplate.opsForHash().hasKey(key, hashKey);
            boolean result = exists != null && exists;
            log.info("Redis hhasKey: {} -> {}: {}", key, hashKey, result);
            return result;
        } catch (Exception e) {
            log.error("Redis hhasKey 异常, key: {}, hashKey: {}", key, hashKey, e);
            return false;
        }
    }

    // ============================ List 操作 ============================

    /**
     * List缓存放入（从左侧插入）
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean lpush(String key, Object value) {
        if (key == null || key.isEmpty() || value == null) {
            log.warn("Redis lpush: key或value参数为空");
            return false;
        }
        try {
            redisTemplate.opsForList().leftPush(key, value);
            log.info("Redis lpush: {} -> {}", key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lpush 异常, key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * List缓存放入（从右侧插入）
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean rpush(String key, Object value) {
        if (key == null || key.isEmpty() || value == null) {
            log.warn("Redis rpush: key或value参数为空");
            return false;
        }
        try {
            redisTemplate.opsForList().rightPush(key, value);
            log.info("Redis rpush: {} -> {}", key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis rpush 异常, key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 获取List缓存内容
     * @param key 键
     * @param start 开始索引（0开始）
     * @param end 结束索引（-1表示所有）
     * @return List集合
     */
    public List<Object> lrange(String key, long start, long end) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis lrange: key参数为空");
            return Collections.emptyList();
        }
        try {
            List<Object> list = redisTemplate.opsForList().range(key, start, end);
            log.info("Redis lrange: {} -> 范围[{},{}] -> 大小{}", key, start, end, list.size());
            return list;
        } catch (Exception e) {
            log.error("Redis lrange 异常, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取List缓存长度
     * @param key 键
     * @return 长度
     */
    public long llen(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis llen: key参数为空");
            return 0;
        }
        try {
            Long length = redisTemplate.opsForList().size(key);
            log.info("Redis llen: {} -> {}", key, length);
            return length == null ? 0 : length;
        } catch (Exception e) {
            log.error("Redis llen 异常, key: {}", key, e);
            return 0;
        }
    }

    // ============================ Set 操作 ============================

    /**
     * Set缓存放入
     * @param key 键
     * @param values 值（可传多个）
     * @return 成功添加的数量
     */
    public long sAdd(String key, Object... values) {
        if (key == null || key.isEmpty() || values == null || values.length == 0) {
            log.warn("Redis sAdd: key或values参数为空");
            return 0;
        }
        try {
            Long addCount = redisTemplate.opsForSet().add(key, values);
            log.info("Redis sAdd: {} -> {} -> 添加数量{}", key, Arrays.toString(values), addCount);
            return addCount == null ? 0 : addCount;
        } catch (Exception e) {
            log.error("Redis sAdd 异常, key: {}, values: {}", key, Arrays.toString(values), e);
            return 0;
        }
    }

    /**
     * 获取Set缓存所有值
     * @param key 键
     * @return Set集合
     */
    public Set<Object> sMembers(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis sMembers: key参数为空");
            return Collections.emptySet();
        }
        try {
            Set<Object> set = redisTemplate.opsForSet().members(key);
            log.info("Redis sMembers: {} -> 大小{}", key, set.size());
            return set;
        } catch (Exception e) {
            log.error("Redis sMembers 异常, key: {}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 判断value是否在Set缓存中
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    public boolean sIsMember(String key, Object value) {
        if (key == null || key.isEmpty() || value == null) {
            log.warn("Redis sIsMember: key或value参数为空");
            return false;
        }
        try {
            Boolean exists = redisTemplate.opsForSet().isMember(key, value);
            boolean result = exists != null && exists;
            log.info("Redis sIsMember: {} -> {}: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Redis sIsMember 异常, key: {}, value: {}", key, value, e);
            return false;
        }
    }

    // ============================ ZSet 操作（有序集合） ============================

    /**
     * ZSet缓存放入
     * @param key 键
     * @param value 值
     * @param score 分数（用于排序）
     * @return 是否成功
     */
    public boolean zAdd(String key, Object value, double score) {
        if (key == null || key.isEmpty() || value == null) {
            log.warn("Redis zAdd: key或value参数为空");
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            boolean success = result != null && result;
            log.info("Redis zAdd: {} -> {} (score:{}) -> {}", key, value, score, success);
            return success;
        } catch (Exception e) {
            log.error("Redis zAdd 异常, key: {}, value: {}, score: {}", key, value, score, e);
            return false;
        }
    }

    /**
     * 获取ZSet缓存指定范围的值（按分数升序）
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引（-1表示所有）
     * @return ZSet集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis zRange: key参数为空");
            return Collections.emptySet();
        }
        try {
            Set<Object> set = redisTemplate.opsForZSet().range(key, start, end);
            log.info("Redis zRange: {} -> 范围[{},{}] -> 大小{}", key, start, end, set.size());
            return set;
        } catch (Exception e) {
            log.error("Redis zRange 异常, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet缓存指定范围的值（按分数降序）
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引（-1表示所有）
     * @return ZSet集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        if (key == null || key.isEmpty()) {
            log.warn("Redis zReverseRangeWithScores: key参数为空");
            return Collections.emptySet();
        }
        try {
            Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            log.info("Redis zReverseRangeWithScores: {} -> 范围[{},{}] -> 大小{}", key, start, end, set.size());
            return set;
        } catch (Exception e) {
            log.error("Redis zReverseRangeWithScores 异常, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptySet();
        }
    }
}