package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.product.mapper.CosmeticsMapper;
import com.example.silverpear.product.productdto.CosmeticsDto;

import com.example.silverpear.service.CosmeticsService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/cosmetics")
public class CosmeticsController {

    @Autowired
    private CosmeticsService cosmeticsService;
    @Autowired
    private CosmeticsMapper cosmeticsMapper;

    @GetMapping
    public ResponseEntity<List<Cosmetics>> getAllCosmetics() {
        List<Cosmetics> cosmetics = cosmeticsService.findAllCosmetics();
        return ResponseEntity.ok(cosmetics);
    }

    @PostMapping
    public ResponseEntity<Cosmetics> saveCosmetics(@RequestBody Cosmetics cosmetics) {
        Cosmetics savedCosmetics = cosmeticsService.create(cosmetics);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCosmetics);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCosmeticsById(@PathVariable Long id) {
        try {
            cosmeticsService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CosmeticsDto> updateCosmetics(
            @PathVariable Long id,
            @RequestBody CosmeticsDto dto) {

        Cosmetics cosmetics = cosmeticsMapper.toEntity(dto);
        Cosmetics updatedCosmetics = cosmeticsService.updateCosmetics(id, cosmetics);

        return ResponseEntity.ok(cosmeticsMapper.toDto(updatedCosmetics));
    }

}