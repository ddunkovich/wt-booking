/*
 * © 2025 Danil Dunkovich. All rights reserved.
 * Provided for evaluation purposes only.
 * Any commercial use, distribution, or modification requires explicit permission.
 */

package com.wtplanner.wtbooking.listener;

import com.wtplanner.wtbooking.service.CacheService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Marks cache invalid at startup because liquibase loads new data */
@Component
public class CacheWarmupListener {
    private final CacheService cacheService;

    public CacheWarmupListener(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void invalidateCacheOnStartup() {
        cacheService.invalidate();
    }
}
