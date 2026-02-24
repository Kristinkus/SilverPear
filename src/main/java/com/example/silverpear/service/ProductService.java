package com.example.silverpear.service;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт с id " + id + " не найден"));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> searchProducts(String name, String brand, String category) {
        return productRepository.searchProducts(name, brand, category);
    }
}