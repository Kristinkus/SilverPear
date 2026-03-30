package com.example.silverpear.api;

import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.product.productdto.PerfumeDto;
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

@RequestMapping("/api/perfumes")
@Tag(name = "Парфюм", description = "Товары категории парфюм")
public interface PerfumeApi {

    @GetMapping
    @Operation(summary = "Все позиции парфюма")
    ResponseEntity<List<Perfume>> getAllPerfumes();

    @PostMapping
    @Operation(summary = "Создать парфюм")
    ResponseEntity<PerfumeDto> savePerfume(@RequestBody PerfumeDto perfumeDto);

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить по id")
    ResponseEntity<Void> deletePerfumeById(@PathVariable Long id);

    @PutMapping("/{id}")
    @Operation(summary = "Обновить парфюм")
    ResponseEntity<PerfumeDto> updatePerfume(
            @PathVariable Long id,
            @RequestBody PerfumeDto dto);
}
