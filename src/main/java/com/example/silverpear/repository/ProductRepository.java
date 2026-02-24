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
        for (Product product : products) {
            if (product.getId() == id) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public void deleteById(int id) {
        products.removeIf(p -> p.getId() == id);
    }

    public Product save(Product product) {
        if (product.getId() == 0) {
            product.setId(nextId++);
            products.add(product);
        } else {
            deleteById(product.getId());
            products.add(product);
        }
        return product;
    }

    public List<Product> searchProducts(String name, String brand, String category) {
        List<Product> result = new ArrayList<>();

        for (Product product : products) {
            boolean match = true;

            if (name != null && !name.isEmpty()) {
                if (!product.getName().toLowerCase().contains(name.toLowerCase())) {
                    continue;
                }
            }

            if (match && brand != null && !brand.isEmpty()) {
                if (!product.getBrand().toLowerCase().contains(brand.toLowerCase())) {
                    continue;
                }
            }

            if (match && category != null && !category.isEmpty()) {
                if (!product.getCategory().toLowerCase().contains(category.toLowerCase())) {
                    continue;
                }
            }

            if (match) {
                result.add(product);
            }
        }

        return result;
    }
}