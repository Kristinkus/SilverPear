package com.example.silverpear.service;

import com.example.silverpear.enums.SkinType;
import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.repository.CosmeticsRepository;
import com.example.silverpear.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CosmeticsServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CosmeticsRepository cosmeticsRepository;
    @Mock
    private CacheService cacheService;

    private CosmeticsService cosmeticsService;

    @BeforeEach
    void setUp() {
        cosmeticsService = new CosmeticsService(productRepository, cosmeticsRepository, cacheService);
    }

    @Test
    void create_success() {
        Cosmetics cosmetics = new Cosmetics();
        when(cosmeticsRepository.save(cosmetics)).thenReturn(cosmetics);
        assertSame(cosmetics, cosmeticsService.create(cosmetics));
    }

    @Test
    void findAllCosmetics_success() {
        List<Cosmetics> list = List.of(new Cosmetics());
        when(cosmeticsRepository.findAll()).thenReturn(list);
        assertSame(list, cosmeticsService.findAllCosmetics());
    }

    @Test
    void delete_success() {
        Cosmetics cosmetics = new Cosmetics();
        cosmeticsService.delete(cosmetics);
        verify(cosmeticsRepository).delete(cosmetics);
    }

    @Test
    void updateCosmetics_success() {
        Cosmetics existing = new Cosmetics();
        existing.setId(1L);
        Cosmetics incoming = new Cosmetics();
        incoming.setName("n");
        incoming.setBrand("b");
        incoming.setDescription("d");
        incoming.setCategory("c");
        incoming.setSalePrice(99.0);
        incoming.setInStock(true);
        incoming.setType("t");
        incoming.setPrescription("rx");
        incoming.setSkinType(SkinType.DRY);
        incoming.setFinish("matte");

        when(cosmeticsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(cosmeticsRepository.save(existing)).thenReturn(existing);

        Cosmetics updated = cosmeticsService.updateCosmetics(1L, incoming);
        assertEquals("n", updated.getName());
        assertEquals("rx", updated.getPrescription());
        assertEquals(SkinType.DRY, updated.getSkinType());
        assertEquals("matte", updated.getFinish());
    }

    @Test
    void updateCosmetics_notFound() {
        when(cosmeticsRepository.findById(1L)).thenReturn(Optional.empty());
        Cosmetics incoming = new Cosmetics();
        assertThrows(RuntimeException.class, () -> cosmeticsService.updateCosmetics(1L, incoming));
    }
}
