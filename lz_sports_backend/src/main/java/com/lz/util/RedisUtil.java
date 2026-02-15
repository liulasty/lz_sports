package com.lz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Redis Utility (Placeholder)
 */
@Component
public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    // TODO: Implement Redis operations
    public void set(String key, Object value) {
        log.info("Redis set: {} -> {}", key, value);
    }

    public Object get(String key) {
        log.info("Redis get: {}", key);
        return null;
    }
}
