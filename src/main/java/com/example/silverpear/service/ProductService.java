package com.example.silverpear.service;

import com.example.silverpear.enums.Gender;
import com.example.silverpear.cache.CacheKey;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.enums.ErrorMessages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductService {

    private static final String CACHE_ENTITY_PRODUCT = "Product";
    private static final String CACHE_KEY_FIND_ALL = CACHE_ENTITY_PRODUCT + ":findAll";
    private static final String CACHE_KEY_FIND_BY_CATEGORY = CACHE_ENTITY_PRODUCT + ":findByCategory";
    private static final String CACHE_METHOD_FIND_BY_ID = "findById";

    protected final ProductRepository productRepository;
    protected final CacheService cacheService;

    public ProductService(ProductRepository productRepository, CacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    public List<Product> findAll() {
        CacheKey key = new CacheKey(
                CACHE_ENTITY_PRODUCT,
                "findAll",
                "",
                0, 0,
                "id",
                "asc"
        );

        List<Product> cached = cacheService.get(key);
        if (cached != null) {
            return cached;
        }

        List<Product> products = productRepository.findAll();
        cacheService.put(key, products);
        return products;
    }

    public Page<Product> findAll(Pageable pageable) {
        CacheKey key = new CacheKey(
                CACHE_ENTITY_PRODUCT,
                "findAll",
                "",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                "id",
                "asc"
        );

        Page<Product> cached = cacheService.get(key);
        if (cached != null) {
            return cached;
        }

        Page<Product> products = productRepository.findAll(pageable);
        cacheService.put(key, products);
        return products;
    }

    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product findById(Long id) {
        CacheKey key = new CacheKey(
                CACHE_ENTITY_PRODUCT,
                CACHE_METHOD_FIND_BY_ID,
                "id=" + id,
                0, 0, "", ""
        );

        Product cached = cacheService.get(key);
        if (cached != null) {
            log.info("Product retrieved from cache: {}", id);
            return cached;
        }

        log.info("Product not in cache: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.PRODUCT_NOT_FOUND.withId(id)));

        cacheService.put(key, product);
        log.info("Product saved to cache: {}", id);

        return product;
    }

    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public Product create(Product product) {
        Product saved = productRepository.save(product);

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        log.info("Cache invalidated after product creation");

        return saved;
    }

    public List<Product> searchProducts(String name, String brand, String category) {
        List<Product> products = new ArrayList<>();
        if (name != null) {
            products.addAll(productRepository.findByName(name));
        }
        if (brand != null) {
            products.addAll(productRepository.findByBrand(brand));
        }
        if (category != null) {
            products.addAll(productRepository.findByCategory(category));
        }
        return products;
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);

        CacheKey key = new CacheKey(CACHE_ENTITY_PRODUCT, CACHE_METHOD_FIND_BY_ID, "id=" + id, 0, 0, "", "");
        cacheService.evict(key);
        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        log.info("Cache invalidated after product deletion: {}", id);
    }

    public Product update(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        updateBaseFields(existingProduct, product);

        Product updated = productRepository.save(existingProduct);

        CacheKey productKey = new CacheKey(CACHE_ENTITY_PRODUCT, CACHE_METHOD_FIND_BY_ID, "id=" + id, 0, 0, "", "");
        cacheService.put(productKey, updated);
        log.info("Updated product saved to cache: {}", id);

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_CATEGORY);
        log.info("Cache invalidated after product update: {}", id);

        return updated;
    }

    protected void updateBaseFields(Product existing, Product source) {
        existing.setName(source.getName());
        existing.setBrand(source.getBrand());
        existing.setDescription(source.getDescription());
        existing.setCategory(source.getCategory());
        existing.setSalePrice(source.getSalePrice());
        existing.setInStock(source.isInStock());
        existing.setType(source.getType());
        existing.setGender(source.getGender());
        existing.setVolume(source.getVolume());
    }

    @Transactional
    public Product patchUpdate(Long id, Map<String, Object> updates) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        updates.forEach((key, value) -> {
            if (value == null) {
                return;
            }

            switch (key) {
                case "name":
                    existingProduct.setName((String) value);
                    break;
                case "brand":
                    existingProduct.setBrand((String) value);
                    break;
                case "description":
                    existingProduct.setDescription((String) value);
                    break;
                case "category":
                    existingProduct.setCategory((String) value);
                    break;
                case "salePrice":
                    existingProduct.setSalePrice(((Number) value).doubleValue());
                    break;
                case "inStock":
                    existingProduct.setInStock((Boolean) value);
                    break;
                case "productType":
                    existingProduct.setType((String) value);
                    break;
                case "gender":
                    existingProduct.setGender(Gender.valueOf((String) value));
                    break;
                case "volume":
                    existingProduct.setVolume(((Number) value).doubleValue());
                    break;
                default:
                    break;
            }
        });

        Product updated = productRepository.save(existingProduct);

        CacheKey productKey = new CacheKey(CACHE_ENTITY_PRODUCT, CACHE_METHOD_FIND_BY_ID, "id=" + id, 0, 0, "", "");
        cacheService.put(productKey, updated);
        log.info("Patched product saved to cache: {}", id);

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_CATEGORY);
        log.info("Cache invalidated after product patch update: {}", id);

        return updated;
    }
    public List<Product> searchInRange(Double lowPrice, Double highPrice) {
        return productRepository.findInRange(lowPrice, highPrice);
    }
}