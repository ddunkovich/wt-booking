package com.wtplanner.wtbooking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final StringRedisTemplate redis;

    private static final String COUNT_KEY = "available_units_count";
    private static final String VALID_FLAG_KEY = "available_units_cache_valid";

    /** Mark cache as invalid (e.g. on startup) */
    public void invalidate() {
        redis.opsForValue().set(VALID_FLAG_KEY, "false");
    }

    /** Check if cache is valid */
    public boolean isValid() {
        return Boolean.parseBoolean(redis.opsForValue().get(VALID_FLAG_KEY));
    }

    /** Get cached count or null if missing */
    public Long getCachedCount() {
        String value = redis.opsForValue().get(COUNT_KEY);
        return value != null ? Long.parseLong(value) : null;
    }

    /** Set count and mark cache valid */
    public void updateCount(long count) {
        redis.opsForValue().set(COUNT_KEY, String.valueOf(count));
        redis.opsForValue().set(VALID_FLAG_KEY, "true");
    }

    /** Increment or decrement cached count if cache is valid */
    public void adjustCount(int delta, long fallbackCount) {
        String current = redis.opsForValue().get(COUNT_KEY);
        if (current != null && isValid()) {
            long newCount = Long.parseLong(current) + delta;
            if (newCount < 0) newCount = 0;
            redis.opsForValue().set(COUNT_KEY, String.valueOf(newCount));
        } else {
            // Rebuild cache from database count
            updateCount(fallbackCount);
        }
    }
}

