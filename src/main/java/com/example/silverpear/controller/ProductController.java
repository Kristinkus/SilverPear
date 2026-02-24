package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productdto.UserDto;
import com.example.silverpear.product.productdto.UserMapper;
import com.example.silverpear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
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
    public ResponseEntity<UserDto> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(savedProduct));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {

        List<Product> products = productService.searchProducts(name, brand, category);
        return ResponseEntity.ok(userMapper.toDtoList(products));
    }
}