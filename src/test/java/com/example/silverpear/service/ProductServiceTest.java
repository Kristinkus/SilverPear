package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import com.example.silverpear.enums.Gender;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private CacheService cacheService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, favoriteRepository, cacheService);
    }

    @Test
    void findAll_cached() {
        List<Product> cached = List.of(new Product());
        when(cacheService.get(any(CacheKey.class))).thenReturn(cached);
        assertSame(cached, productService.findAll());
    }

    @Test
    void findAll_fromRepository() {
        List<Product> products = List.of(new Product(), new Product());
        when(cacheService.get(any(CacheKey.class))).thenReturn(null);
        when(productRepository.findAll()).thenReturn(products);
        assertSame(products, productService.findAll());
        verify(cacheService).put(any(CacheKey.class), eq(products));
    }

    @Test
    void findAllPage_cachedAndRepo() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Product> cached = new PageImpl<>(List.of(new Product()));
        when(cacheService.get(any(CacheKey.class))).thenReturn(cached);
        assertSame(cached, productService.findAll(pageable));

        Page<Product> page = new PageImpl<>(List.of(new Product(), new Product()));
        when(cacheService.get(any(CacheKey.class))).thenReturn(null);
        when(productRepository.findAll(pageable)).thenReturn(page);
        assertSame(page, productService.findAll(pageable));
        verify(cacheService).put(any(CacheKey.class), eq(page));
    }

    @Test
    void findByBrandCategoryName_forwardCalls() {
        List<Product> list = List.of(new Product());
        when(productRepository.findByBrand("b")).thenReturn(list);
        when(productRepository.findByCategory("c")).thenReturn(list);
        when(productRepository.findByName("n")).thenReturn(list);
        assertSame(list, productService.findByBrand("b"));
        assertSame(list, productService.findByCategory("c"));
        assertSame(list, productService.findByName("n"));
    }

    @Test
    void findById_cachedAndRepositoryAndNotFound() {
        Product cached = new Product();
        when(cacheService.get(any(CacheKey.class))).thenReturn(cached);
        assertSame(cached, productService.findById(1L));

        Product repoProduct = new Product();
        when(cacheService.get(any(CacheKey.class))).thenReturn(null);
        when(productRepository.findById(2L)).thenReturn(Optional.of(repoProduct));
        assertSame(repoProduct, productService.findById(2L));
        verify(cacheService).put(any(CacheKey.class), eq(repoProduct));

        when(cacheService.get(any(CacheKey.class))).thenReturn(null);
        when(productRepository.findById(3L)).thenReturn(Optional.empty());
        long missingId = 3L;
        assertThrows(RuntimeException.class, () -> productService.findById(missingId));
    }

    @Test
    void create_deleteById_update_successAndNotFound() {
        Product created = new Product();
        when(productRepository.save(created)).thenReturn(created);
        assertSame(created, productService.create(created));
        verify(cacheService).evictByPattern("Product:findAll");

        productService.deleteById(1L);
        verify(productRepository).deleteById(1L);
        verify(cacheService).evict(any(CacheKey.class));

        Product existing = new Product();
        existing.setId(10L);
        Product incoming = new Product();
        incoming.setName("name");
        incoming.setBrand("brand");
        incoming.setDescription("desc");
        incoming.setCategory("cat");
        incoming.setSalePrice(123.0);
        incoming.setInStock(true);
        incoming.setType("type");
        incoming.setGender(Gender.FEMALE);
        incoming.setVolume(55.0);
        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product updated = productService.update(10L, incoming);
        assertEquals("name", updated.getName());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals(55.0, updated.getVolume());

        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        long missingProductId = 999L;
        assertThrows(RuntimeException.class, () -> productService.update(missingProductId, incoming));
    }

    @Test
    void searchProducts_and_searchInRange() {
        List<Product> byName = List.of(new Product());
        List<Product> byBrand = List.of(new Product());
        List<Product> byCategory = List.of(new Product());
        when(productRepository.findByName("n")).thenReturn(byName);
        when(productRepository.findByBrand("b")).thenReturn(byBrand);
        when(productRepository.findByCategory("c")).thenReturn(byCategory);

        List<Product> result = productService.searchProducts("n", "b", "c");
        assertEquals(3, result.size());

        List<Product> range = List.of(new Product());
        when(productRepository.findInRange(10.0, 20.0)).thenReturn(range);
        assertSame(range, productService.searchInRange(10.0, 20.0));
    }

    @Test
    void searchProducts_allNull_doesNotQueryRepository() {
        assertTrue(productService.searchProducts(null, null, null).isEmpty());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchUpdate_allBranchesAndNullAndDefault() {
        Product existing = new Product();
        existing.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "n");
        updates.put("brand", "b");
        updates.put("description", "d");
        updates.put("category", "c");
        updates.put("salePrice", 15.5);
        updates.put("inStock", true);
        updates.put("productType", "type");
        updates.put("gender", "MALE");
        updates.put("volume", 30);
        updates.put("unknown", "ignored");
        updates.put("nameNull", null);

        Product updated = productService.patchUpdate(1L, updates);
        assertEquals("n", updated.getName());
        assertEquals("b", updated.getBrand());
        assertEquals("d", updated.getDescription());
        assertEquals("c", updated.getCategory());
        assertEquals(15.5, updated.getSalePrice());
        assertEquals("type", updated.getType());
        assertEquals(Gender.MALE, updated.getGender());
        assertEquals(30.0, updated.getVolume());

        ArgumentCaptor<CacheKey> captor = ArgumentCaptor.forClass(CacheKey.class);
        verify(cacheService).put(captor.capture(), any(Product.class));

        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        Map<String, Object> empty = Map.of();
        assertThrows(RuntimeException.class, () -> productService.patchUpdate(2L, empty));
    }
}
