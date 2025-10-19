package com.wtplanner.wtbooking.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {
    // Spring Boot autoconfigures RedisCacheManager automatically
}