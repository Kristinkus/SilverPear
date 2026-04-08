package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CacheServiceTest {

    private final CacheService cacheService = new CacheService();

    @Test
    void putAndGetAndEvict_workCorrectly() {
        CacheKey key = new CacheKey("Product", "findById", "id=1", 0, 0, "", "");
        cacheService.put(key, "value");
        assertEquals("value", cacheService.get(key));

        cacheService.evict(key);
        assertNull(cacheService.get(key));
    }

    @Test
    void evict_whenKeyAbsent_doesNotFail() {
        CacheKey key = new CacheKey("Product", "findById", "id=99", 0, 0, "", "");
        cacheService.evict(key);
        assertNull(cacheService.get(key));
    }

    @Test
    void get_whenNoValue_returnsNull() {
        CacheKey key = new CacheKey("Order", "findById", "id=2", 0, 0, "", "");
        assertNull(cacheService.get(key));
    }

    @Test
    void evictByPattern_removesMatchingOnly() {
        CacheKey key1 = new CacheKey("Order", "findAll", "", 0, 0, "", "");
        CacheKey key2 = new CacheKey("Product", "findAll", "", 0, 0, "", "");
        cacheService.put(key1, 1);
        cacheService.put(key2, 2);

        cacheService.evictByPattern("Order");

        assertNull(cacheService.get(key1));
        assertEquals(Integer.valueOf(2), cacheService.get(key2));
    }
}
