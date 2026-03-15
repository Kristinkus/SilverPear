package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
class CacheService {

    private final Map<CacheKey, Object> cache = new ConcurrentHashMap<>();

    private int hits = 0;
    private int misses = 0;

    @SuppressWarnings("unchecked")
    public <T> T get(CacheKey key, Class<T> type) {
        if (cache.containsKey(key)) {
            hits++;
            log.info("Получено из кэша", key.getEntityType(), key);
            return (T) cache.get(key);
        }
        misses++;
        return null;
    }

    public void put(CacheKey key, Object data) {
        cache.put(key, data);
        log.info("Сохранено в кэш", key.getEntityType(), key);
    }

    public void clearByEntity(String entityType) {
        cache.keySet().removeIf(key -> key.getEntityType().equals(entityType));
        log.info("Кэш очищен для {}", entityType);
    }

    // Очистить все
    public void clearAll() {
        cache.clear();
        hits = 0;
        misses = 0;
        log.info("Весь кэш очищен --E");
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", cache.size());
        stats.put("hits", hits);
        stats.put("misses", misses);
        return stats;
    }
}
