package com.example.silverpear.service;

import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.PerfumeRepository;
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
class PerfumeServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private PerfumeRepository perfumeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private CacheService cacheService;

    private PerfumeService perfumeService;

    @BeforeEach
    void setUp() {
        perfumeService = new PerfumeService(
                productRepository, perfumeRepository, favoriteRepository, cacheService);
    }

    @Test
    void create_success() {
        Perfume perfume = new Perfume();
        when(perfumeRepository.save(perfume)).thenReturn(perfume);
        assertSame(perfume, perfumeService.create(perfume));
    }

    @Test
    void findAllPerfume_success() {
        List<Perfume> list = List.of(new Perfume());
        when(perfumeRepository.findAll()).thenReturn(list);
        assertSame(list, perfumeService.findAllPerfume());
    }

    @Test
    void delete_success() {
        Perfume perfume = new Perfume();
        perfumeService.delete(perfume);
        verify(perfumeRepository).delete(perfume);
    }

    @Test
    void updatePerfume_success() {
        Perfume existing = new Perfume();
        existing.setId(1L);
        Perfume incoming = new Perfume();
        incoming.setName("n");
        incoming.setBrand("b");
        incoming.setDescription("d");
        incoming.setCategory("c");
        incoming.setSalePrice(11.0);
        incoming.setInStock(true);
        incoming.setType("t");
        incoming.setTopNotes(List.of("top"));
        incoming.setMiddleNotes(List.of("mid"));
        incoming.setBaseNotes(List.of("base"));

        when(perfumeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(perfumeRepository.save(existing)).thenReturn(existing);

        Perfume updated = perfumeService.updatePerfume(1L, incoming);
        assertEquals("n", updated.getName());
        assertEquals(List.of("top"), updated.getTopNotes());
        assertEquals(List.of("mid"), updated.getMiddleNotes());
        assertEquals(List.of("base"), updated.getBaseNotes());
    }

    @Test
    void updatePerfume_notFound() {
        when(perfumeRepository.findById(1L)).thenReturn(Optional.empty());
        Perfume incoming = new Perfume();
        assertThrows(RuntimeException.class, () -> perfumeService.updatePerfume(1L, incoming));
    }
}
