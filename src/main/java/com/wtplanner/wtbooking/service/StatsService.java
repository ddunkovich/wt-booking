package com.wtplanner.wtbooking.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/** Простая статистика в кеше: общее количество бронирований и суммарная выручка. */
@Service
public class StatsService {
    private final AtomicLong count = new AtomicLong(0);
    private double revenue = 0d;

    @CachePut(cacheNames = "stats")
    public synchronized void recordBooking(double amount) {
        count.incrementAndGet();
        revenue = revenue + amount;
    }

    @Cacheable(cacheNames = "stats")
    public synchronized Stats snapshot() {
        return new Stats(count.get(), revenue);
    }

    public record Stats(long bookings, double revenue) {}
}
