package com.example.silverpear.controller;

import com.example.silverpear.api.ProductApi;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.mapper.ProductMapper;
import com.example.silverpear.product.productdto.ProductDto;
import com.example.silverpear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Override
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDto = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDto);
    }

    @Override
    public ResponseEntity<ProductDto> getProductById(Long id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(productMapper.toDto(product));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @Override
    public ResponseEntity<ProductDto> createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDto(savedProduct));
    }

    @Override
    public ResponseEntity<List<ProductDto>> searchProducts(String name, String brand, String category) {
        List<Product> products = productService.searchProducts(name, brand, category);
        return ResponseEntity.ok(productMapper.toDtoList(products));
    }

    @Override
    public ResponseEntity<Void> deleteProductById(Long id) {
        try {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
    }

    @Override
    public ResponseEntity<ProductDto> updateProduct(Long id, ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setId(id);
        Product updatedProduct = productService.update(product.getId(), product);
        return ResponseEntity.ok(productMapper.toDto(updatedProduct));
    }

    @Override
    public ResponseEntity<ProductDto> patchProduct(Long id, Map<String, Object> updates) {
        Product updatedProduct = productService.patchUpdate(id, updates);
        return ResponseEntity.ok(productMapper.toDto(updatedProduct));
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsInRange(Double lowPrice, Double highPrice) {
        List<Product> productsInRange = productService.searchInRange(lowPrice, highPrice);
        return ResponseEntity.ok(productMapper.toDtoList(productsInRange));
    }

    @Override
    public ResponseEntity<Page<ProductDto>> getProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findAll(pageable);
        Page<ProductDto> dtoPage = productPage.map(productMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }
}
