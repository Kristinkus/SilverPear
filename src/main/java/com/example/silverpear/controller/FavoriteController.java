package com.example.silverpear.controller;

import com.example.silverpear.api.FavoriteApi;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.product.productdto.ProductSimpleDto;
import com.example.silverpear.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class FavoriteController implements FavoriteApi {

    private final FavoriteService favoriteService;
    private final ProductMapper productMapper;

    @Override
    public ResponseEntity<Set<ProductSimpleDto>> getFavorites(Long userId) {
        Set<Product> favorites = favoriteService.getFavorites(userId);
        Set<ProductSimpleDto> dtos = favorites.stream()
                .map(productMapper::toSimpleDto)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<Void> addFavorite(Long userId, Long productId) {
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeFavorite(Long userId, Long productId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
