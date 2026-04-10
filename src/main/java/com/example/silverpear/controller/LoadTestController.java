package com.example.silverpear.controller;

import com.example.silverpear.api.LoadTestApi;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.product.productdto.ProductDto;
import com.example.silverpear.service.LoadTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoadTestController implements LoadTestApi {

    private final LoadTestService loadTestService;
    private final ProductMapper productMapper;

    @Override
    public ResponseEntity<ProductDto> createLoadTestProduct() {
        Product product = loadTestService.createLoadTestProduct();
        return ResponseEntity.ok(productMapper.toDto(product));
    }
}
