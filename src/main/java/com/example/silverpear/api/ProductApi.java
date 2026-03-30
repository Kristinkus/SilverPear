package com.example.silverpear.api;

import com.example.silverpear.product.productdto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/products")
@Tag(name = "Товары", description = "Каталог продуктов")
public interface ProductApi {

    @GetMapping
    @Operation(summary = "Все товары")
    ResponseEntity<List<ProductDto>> getAllProducts();

    @GetMapping("/{id}")
    @Operation(summary = "Товар по id")
    ResponseEntity<ProductDto> getProductById(@PathVariable Long id);

    @PostMapping
    @Operation(summary = "Создать товар")
    ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto);

    @GetMapping("/search")
    @Operation(summary = "Поиск по имени, бренду, категории")
    ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category);

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар")
    ResponseEntity<Void> deleteProductById(@PathVariable Long id);

    @PutMapping("/{id}")
    @Operation(summary = "Полное обновление товара")
    ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto dto);

    @PatchMapping("/{id}")
    @Operation(summary = "Частичное обновление полей")
    ResponseEntity<ProductDto> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates);

    @GetMapping("/in-range")
    @Operation(summary = "Товары в диапазоне цен")
    ResponseEntity<List<ProductDto>> getProductsInRange(
            @RequestParam Double lowPrice,
            @RequestParam Double highPrice);

    @GetMapping("/page")
    @Operation(summary = "Постраничный список")
    ResponseEntity<Page<ProductDto>> getProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size);
}
