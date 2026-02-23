package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Product;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {


    private final List<Product> products = new ArrayList<>();
    private int nextId = 1;

    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public Optional<Product> findById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    public Product save(Product product) {
        if (product.getId() == 0) {
            product.setId(nextId++);
            products.add(product);
        } else {
            // Обновление существующего
            deleteById(product.getId());
            products.add(product);
        }
        return product;
    }

    public void deleteById(int id) {
        products.removeIf(p -> p.getId() == id);
    }

    public List<Product> findByBrand(String brand) {
        return products.stream()
                .filter(p -> p.getBrand().equals(brand))
                .toList();
    }

    public List<Product> findByPriceLessThan(double price) {
        return products.stream()
                .filter(p -> p.getPrice() < price)
                .toList();
    }

    public List<Product> findByCategory(String category) {
        return products.stream()
                .filter(p -> p.getCategory().equals(category))
                .toList();
    }

    public boolean existsByName(String name) {
        return products.stream()
                .anyMatch(p -> p.getName().equals(name));
    }
    public List<Product> searchProducts(String name, String brand, String category) {
        return products.stream()
                .filter(product -> {
                    boolean match = true;
                    if (name != null && !name.isEmpty()) {
                        match = match && product.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    if (brand != null && !brand.isEmpty()) {
                        match = match && product.getBrand().toLowerCase().contains(brand.toLowerCase());
                    }
                    if (category != null && !category.isEmpty()) {
                        match = match && product.getCategory().toLowerCase().contains(category.toLowerCase());
                    }
                    return match;
                })
                .collect(Collectors.toList());
    }
}