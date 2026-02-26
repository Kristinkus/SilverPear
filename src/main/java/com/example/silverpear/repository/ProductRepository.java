package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Product;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            if (allMatch(product, name, brand, category)) {
                result.add(product);
            }
        }

        return result;
    }

    private boolean allMatch(Product product, String name, String brand, String category) {
        return Optional.ofNullable(name).filter(n -> !n.isEmpty())
                .map(n -> product.getName().toLowerCase().contains(n.toLowerCase()))
                .orElse(true) &&
                Optional.ofNullable(brand).filter(b -> !b.isEmpty())
                        .map(b -> product.getBrand().toLowerCase().contains(b.toLowerCase()))
                        .orElse(true) &&
                Optional.ofNullable(category).filter(c -> !c.isEmpty())
                        .map(c -> product.getCategory().toLowerCase().contains(c.toLowerCase()))
                        .orElse(true);
    }
}