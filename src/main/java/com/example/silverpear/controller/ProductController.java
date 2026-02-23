package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productDto.UserDto;
import com.example.silverpear.product.productDto.UserMapper;
import com.example.silverpear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(userMapper.toDtoList(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getProductById(@PathVariable int id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(userMapper.toDto(product));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @PostMapping
    public ResponseEntity<UserDto> createProduct(@Valid @RequestBody UserDto productDto) {
        Product product = userMapper.toEntity(productDto);
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(savedProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateProduct(
            @PathVariable int id,
            @Valid @RequestBody UserDto productDto) {
        Product product = userMapper.toEntity(productDto);
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(userMapper.toDto(updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {

        List<Product> products = productService.searchProducts(name, brand, category);
        return ResponseEntity.ok(userMapper.toDtoList(products));
    }

    @GetMapping("/simple")
    public ResponseEntity<List<String>> getSimpleProductList() {
        return ResponseEntity.ok(List.of("Парфюм", "Косметика"));
    }
}