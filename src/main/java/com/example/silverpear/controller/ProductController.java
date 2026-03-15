package com.example.silverpear.controller;


import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.CosmeticsMapper;
import com.example.silverpear.product.productdto.ProductDto;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CosmeticsMapper cosmeticsMapper;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDto = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(productMapper.toDto(product));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDto(savedProduct));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {

        List<Product> products = productService.searchProducts(name, brand, category);
        return ResponseEntity.ok(productMapper.toDtoList(products));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        try {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setId(id);
        Product updatedProduct = productService.update(product.getId(), product);
        return ResponseEntity.ok(productMapper.toDto(updatedProduct));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        Product updatedProduct = productService.patchUpdate(id, updates);
        return ResponseEntity.ok(productMapper.toDto(updatedProduct));
    }


    @GetMapping("/in-range")
    public ResponseEntity<List<ProductDto>> getProductsInRange(
            @RequestParam Double lowPrice,
            @RequestParam Double highPrice) {
        List<Product> productsInRange = productService.searchInRange(lowPrice, highPrice);
        return ResponseEntity.ok(productMapper.toDtoList(productsInRange));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ProductDto>> getProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findAll(pageable);
        Page<ProductDto> dtoPage = productPage.map(productMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }



}

