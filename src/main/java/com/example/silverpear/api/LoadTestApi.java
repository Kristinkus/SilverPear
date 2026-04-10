package com.example.silverpear.api;

import com.example.silverpear.product.productdto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/load-test")
@Tag(name = "Load Test", description = "Эндпоинт для нагрузочного тестирования создания продуктов")
public interface LoadTestApi {

    @PostMapping("/products")
    @Operation(summary = "Создать продукт для нагрузки")
    ResponseEntity<ProductDto> createLoadTestProduct();
}
