package com.example.silverpear.controller;

import com.example.silverpear.api.PerfumeApi;
import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.product.mapper.PerfumeMapper;
import com.example.silverpear.product.productdto.PerfumeDto;
import com.example.silverpear.service.PerfumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerfumeController implements PerfumeApi {

    private final PerfumeService perfumeService;
    private final PerfumeMapper perfumeMapper;

    @Override
    public ResponseEntity<List<Perfume>> getAllPerfumes() {
        List<Perfume> perfumes = perfumeService.findAllPerfume();
        return ResponseEntity.ok(perfumes);
    }

    @Override
    public ResponseEntity<PerfumeDto> savePerfume(PerfumeDto perfumeDto) {
        Perfume perfume = perfumeMapper.toEntity(perfumeDto);
        Perfume savedPerfume = perfumeService.create(perfume);
        PerfumeDto savedDto = perfumeMapper.toDto(savedPerfume);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @Override
    public ResponseEntity<Void> deletePerfumeById(Long id) {
        try {
            perfumeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfume not found with id: " + id);
        }
    }

    @Override
    public ResponseEntity<PerfumeDto> updatePerfume(Long id, PerfumeDto dto) {
        Perfume perfume = perfumeMapper.toEntity(dto);
        Perfume updatedPerfume = perfumeService.updatePerfume(id, perfume);
        return ResponseEntity.ok(perfumeMapper.toDto(updatedPerfume));
    }
}
