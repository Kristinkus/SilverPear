package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.product.productdto.ProductSimpleDto;
import com.example.silverpear.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<Set<ProductSimpleDto>> getFavorites(@PathVariable Long userId) {
        Set<Product> favorites = favoriteService.getFavorites(userId);
        Set<ProductSimpleDto> dtos = favorites.stream()
                .map(productMapper::toSimpleDto)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long userId, @PathVariable Long productId) {
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long productId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(favoriteService.isFavorite(userId, productId));
    }
}