package com.example.silverpear.api;

import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.product.productdto.CosmeticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/cosmetics")
@Tag(name = "Косметика", description = "Товары категории косметика")
public interface CosmeticsApi {

    @GetMapping
    @Operation(summary = "Вся косметика")
    ResponseEntity<List<Cosmetics>> getAllCosmetics();

    @PostMapping
    @Operation(summary = "Создать позицию")
    ResponseEntity<CosmeticsDto> saveCosmetics(@RequestBody CosmeticsDto cosmeticsDto);

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить по id")
    ResponseEntity<Void> deleteCosmeticsById(@PathVariable Long id);

    @PutMapping("/{id}")
    @Operation(summary = "Обновить")
    ResponseEntity<CosmeticsDto> updateCosmetics(
            @PathVariable Long id,
            @RequestBody CosmeticsDto dto);
}
