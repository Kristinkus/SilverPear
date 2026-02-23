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
        return productRepository.findAll();  // работает
    }

    public Product getProductById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт с id " + id + " не найден"));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);  // работает
    }

    public Product updateProduct(int id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPurchasePrice(updatedProduct.getPurchasePrice());

        return productRepository.save(existingProduct);  // работает
    }

    public void deleteProduct(int id) {
        productRepository.deleteById(id);  // ИСПРАВЛЕНО: было delete, стало deleteById
    }

    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);  // работает
    }

    public List<Product> getProductsCheaperThan(double price) {
        return productRepository.findByPriceLessThan(price);  // работает
    }
    public List<Product> searchProducts(String name, String brand, String category) {
        return productRepository.searchProducts(name, brand, category);
    }
}