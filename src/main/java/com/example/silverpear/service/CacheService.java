package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CacheService {

    private final Map<CacheKey, Object> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(CacheKey key) {
        if (cache.containsKey(key)) {
            log.info("Получено из кэша: {} {}", key.getEntityType(), key);
            return (T) cache.get(key);
        }
        return null;
    }

    public void put(CacheKey key, Object data) {
        cache.put(key, data);
        log.info("Сохранено в кэш: {} {}", key.getEntityType(), key);
    }

    public void evict(CacheKey key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
            log.info("Cache evicted: {} - {}", key.getEntityType(), key);
        }
    }

    public void evictByPattern(String pattern) {
        cache.keySet().removeIf(key -> key.toString().contains(pattern));
        log.info("Cache evicted by pattern '{}': entries removed", pattern);
    }
}
