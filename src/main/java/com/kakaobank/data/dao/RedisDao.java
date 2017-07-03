package com.kakaobank.data.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RedisDao
 * specific Type은 사용하는 클래스에게 위임.
 *
 * @param <K> the Redis key type against which the template works (usually a String)
 * @param <V> the Redis value type against which the template works
 */
@Repository
public class RedisDao<K, V> {

    @Autowired
    private RedisTemplate<K, V> redisTemplate;

    public Boolean isExist(K key) { return redisTemplate.hasKey(key); }

    public V getValue(K key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setValue(K key, V value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void expire(K key, int timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    public Long rightPush(K key, V value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public V rightPop(K key, V value) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public Long leftPush(K key, V value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public V leftPop(K key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Long size(K key) {
        return redisTemplate.opsForList().size(key);
    }

    public List<V> getRangeList(K key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public long increment(K key, K hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    public long increment(K key, K hashKey) {
        return increment(key, hashKey, 1);
    }

    public Object getHashValue(K key, K hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Object getHashMultiValue(K key, Collection c) {
        return redisTemplate.opsForHash().multiGet(key, c);
    }

    public void setHashValue(K key, K hashkey, V value) {
        redisTemplate.opsForHash().put(key, hashkey, value);
    }

    public void setHashAllValue(K key, Map map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

}