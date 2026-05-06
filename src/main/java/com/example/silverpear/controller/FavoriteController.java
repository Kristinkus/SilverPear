package com.example.silverpear.controller;

import com.example.silverpear.api.FavoriteApi;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.product.productdto.ProductSimpleDto;
import com.example.silverpear.security.AuthPrincipal;
import com.example.silverpear.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class FavoriteController implements FavoriteApi {

    private final FavoriteService favoriteService;
    private final ProductMapper productMapper;
    private final AuthPrincipal authPrincipal;

    private Long resolveTargetUserId(Long requestedUserId) {
        var current = authPrincipal.currentUser();
        if (current.isAdmin()) {
            return requestedUserId;
        }
        return current.getId();
    }

    @Override
    public ResponseEntity<Set<ProductSimpleDto>> getFavorites(Long userId) {
        Long targetUserId = resolveTargetUserId(userId);
        Set<Product> favorites = favoriteService.getFavorites(targetUserId);
        Set<ProductSimpleDto> dtos = favorites.stream()
                .map(productMapper::toSimpleDto)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<Void> addFavorite(Long userId, Long productId) {
        Long targetUserId = resolveTargetUserId(userId);
        favoriteService.addFavorite(targetUserId, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> removeFavorite(Long userId, Long productId) {
        Long targetUserId = resolveTargetUserId(userId);
        favoriteService.removeFavorite(targetUserId, productId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Set<String>> getFavoriteBrands(Long userId) {
        Long targetUserId = resolveTargetUserId(userId);
        return ResponseEntity.ok(favoriteService.getFavoriteBrands(targetUserId));
    }

    @Override
    public ResponseEntity<Void> addFavoriteBrand(Long userId, String brand) {
        Long targetUserId = resolveTargetUserId(userId);
        favoriteService.addFavoriteBrand(targetUserId, brand);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> removeFavoriteBrand(Long userId, String brand) {
        Long targetUserId = resolveTargetUserId(userId);
        favoriteService.removeFavoriteBrand(targetUserId, brand);
        return ResponseEntity.noContent().build();
    }
}
