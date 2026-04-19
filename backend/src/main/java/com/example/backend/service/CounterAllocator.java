package com.example.backend.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CounterAllocator {
    private static final String COUNTER_KEY = "url:global:counter";

    private final StringRedisTemplate redisTemplate;
    private final int batchSize;

    private final AtomicLong current = new AtomicLong(0);
    private final AtomicLong max = new AtomicLong(-1);

    public CounterAllocator(StringRedisTemplate redisTemplate, @Value("${app.counter.batch-size}") int batchSize) {
        this.redisTemplate = redisTemplate;
        this.batchSize = batchSize;
    }

    public long nextId() {
        long value = current.getAndIncrement();
        if (value <= max.get())
            return value;


        synchronized (this) {
            value = current.getAndIncrement();
            if (value <= max.get()) {
                return value;
            }

            Long newMax = redisTemplate.opsForValue().increment(COUNTER_KEY, batchSize);
            if (newMax == null) {
                throw new IllegalStateException("Failed to increment Redis counter.");
            }

            long newMin = newMax - batchSize + 1;
            current.set(newMin + 1);
            max.set(newMax);
            return newMin;
        }
    }
}
