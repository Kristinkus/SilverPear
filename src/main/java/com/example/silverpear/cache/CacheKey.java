package com.example.silverpear.cache;

import lombok.Data;

import java.util.Objects;


@Data
public class CacheKey {

    private final String entityType;

    private final String method;

    private final String params;

    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    public CacheKey(String entityType, String method, String params) {
        this(entityType, method, params, 0, 0, "", "");
    }

    public CacheKey(String entityType, String method, String params,
                    int page, int size, String sortBy, String sortDirection) {
        this.entityType = entityType;
        this.method = method;
        this.params = params;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public static CacheKey forFindById(String entityType, Long id) {
        return new CacheKey(entityType, "findById", "id=" + id);
    }

    public static CacheKey forFindAll(String entityType) {
        return new CacheKey(entityType, "findAll", "");
    }

    public static CacheKey forSearch(String entityType, String... searchParams) {
        String params = String.join("|", searchParams);
        return new CacheKey(entityType, "search", params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheKey cacheKey = (CacheKey) o;

        return page == cacheKey.page &&
                size == cacheKey.size &&
                Objects.equals(entityType, cacheKey.entityType) &&
                Objects.equals(method, cacheKey.method) &&
                Objects.equals(params, cacheKey.params) &&
                Objects.equals(sortBy, cacheKey.sortBy) &&
                Objects.equals(sortDirection, cacheKey.sortDirection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, method, params, page, size, sortBy, sortDirection);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s:%d:%d:%s:%s",
                entityType, method, params, page, size, sortBy, sortDirection);
    }

}