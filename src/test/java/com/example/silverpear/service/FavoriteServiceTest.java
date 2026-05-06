package com.example.silverpear.service;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteService(favoriteRepository, userRepository, productRepository);
    }

    @Test
    void addFavorite_success() {
        User user = new User();
        user.setFavorites(new HashSet<>());
        Product product = new Product();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        favoriteService.addFavorite(1L, 2L);

        assertTrue(user.getFavorites().contains(product));
        verify(userRepository).save(user);
    }

    @Test
    void addFavorite_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> favoriteService.addFavorite(1L, 2L));
    }

    @Test
    void addFavorite_productNotFound() {
        User user = new User();
        user.setFavorites(new HashSet<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> favoriteService.addFavorite(1L, 2L));
    }

    @Test
    void removeFavorite_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        favoriteService.removeFavorite(1L, 2L);
        verify(favoriteRepository).deleteFavoriteLink(1L, 2L);
    }

    @Test
    void removeFavorite_userNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class, () -> favoriteService.removeFavorite(1L, 2L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(favoriteRepository, never()).deleteFavoriteLink(anyLong(), anyLong());
    }

    @Test
    void removeFavorite_deletesLinkWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        favoriteService.removeFavorite(1L, 99L);
        verify(favoriteRepository).deleteFavoriteLink(1L, 99L);
    }

    @Test
    void getFavorites_success() {
        Product p = new Product();
        p.setId(1L);
        p.setName("n");
        p.setBrand("b");
        p.setCategory("c");
        p.setSalePrice(1.0);
        Set<Product> favorites = new HashSet<>(Set.of(p));
        User user = new User();
        user.setFavorites(favorites);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(user));
        assertSame(favorites, favoriteService.getFavorites(1L));
    }

    @Test
    void isFavorite_trueAndFalse() {
        Product product1 = new Product();
        product1.setId(10L);
        when(favoriteRepository.findFavoritesByUserId(1L)).thenReturn(Set.of(product1));
        assertTrue(favoriteService.isFavorite(1L, 10L));
        assertFalse(favoriteService.isFavorite(1L, 99L));
    }
}
