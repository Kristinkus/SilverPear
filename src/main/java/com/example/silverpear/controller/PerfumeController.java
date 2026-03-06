package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.product.mapper.PerfumeMapper;
import com.example.silverpear.product.productdto.PerfumeDto;
import com.example.silverpear.service.PerfumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor

public class PerfumeController {

    private final PerfumeService perfumeService;
    private final PerfumeMapper perfumeMapper;

    @GetMapping
    public ResponseEntity<List<Perfume>> getAllPerfumes() {
        List<Perfume> perfumes = perfumeService.findAllPerfume();
        return ResponseEntity.ok(perfumes);
    }

    @PostMapping
    public ResponseEntity<PerfumeDto> savePerfume(@RequestBody PerfumeDto perfumeDto) {
        Perfume perfume = perfumeMapper.toEntity(perfumeDto);
        Perfume savedPerfume = perfumeService.create(perfume);
        PerfumeDto savedDto = perfumeMapper.toDto(savedPerfume);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerfumeById(@PathVariable Long id) {
        try {
            perfumeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfume not found with id: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerfumeDto> updatePerfume(
            @PathVariable Long id,
            @RequestBody PerfumeDto dto) {

        Perfume perfume = perfumeMapper.toEntity(dto);
        Perfume updatedPerfume = perfumeService.updatePerfume(id, perfume);

        return ResponseEntity.ok(perfumeMapper.toDto(updatedPerfume));
    }
}