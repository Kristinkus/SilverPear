package com.example.silverpear.api;

import com.example.silverpear.product.productdto.ProductSimpleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@RequestMapping("/api/users/{userId}/favorites")
@Tag(name = "Избранное", description = "Избранные товары пользователя")
public interface FavoriteApi {

    @GetMapping
    @Operation(summary = "Список избранного")
    ResponseEntity<Set<ProductSimpleDto>> getFavorites(@PathVariable Long userId);

    @PostMapping("/{productId}")
    @Operation(summary = "Добавить в избранное")
    ResponseEntity<Void> addFavorite(
            @PathVariable Long userId,
            @PathVariable Long productId);

    @DeleteMapping("/{productId}")
    @Operation(summary = "Убрать из избранного")
    ResponseEntity<Void> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long productId);
}
