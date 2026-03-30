package com.example.silverpear.controller;

import com.example.silverpear.api.CosmeticsApi;
import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.product.mapper.CosmeticsMapper;
import com.example.silverpear.product.productdto.CosmeticsDto;
import com.example.silverpear.service.CosmeticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class CosmeticsController implements CosmeticsApi {

    private final CosmeticsService cosmeticsService;
    private final CosmeticsMapper cosmeticsMapper;

    public CosmeticsController(CosmeticsService cosmeticsService, CosmeticsMapper cosmeticsMapper) {
        this.cosmeticsService = cosmeticsService;
        this.cosmeticsMapper = cosmeticsMapper;
    }

    @Override
    public ResponseEntity<List<Cosmetics>> getAllCosmetics() {
        List<Cosmetics> cosmetics = cosmeticsService.findAllCosmetics();
        return ResponseEntity.ok(cosmetics);
    }

    @Override
    public ResponseEntity<CosmeticsDto> saveCosmetics(CosmeticsDto cosmeticsDto) {
        Cosmetics cosmetics = cosmeticsMapper.toEntity(cosmeticsDto);
        Cosmetics savedCosmetics = cosmeticsService.create(cosmetics);
        CosmeticsDto savedDto = cosmeticsMapper.toDto(savedCosmetics);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @Override
    public ResponseEntity<Void> deleteCosmeticsById(Long id) {
        try {
            cosmeticsService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @Override
    public ResponseEntity<CosmeticsDto> updateCosmetics(Long id, CosmeticsDto dto) {
        Cosmetics cosmetics = cosmeticsMapper.toEntity(dto);
        Cosmetics updatedCosmetics = cosmeticsService.updateCosmetics(id, cosmetics);
        return ResponseEntity.ok(cosmeticsMapper.toDto(updatedCosmetics));
    }
}
