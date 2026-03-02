package com.example.silverpear.service;

import com.example.silverpear.enums.Gender;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.example.silverpear.enums.ErrorMessages;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service

public class ProductService {

    protected final ProductRepository productRepository;

    // Конструктор для внедрения зависимости
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.PRODUCT_NOT_FOUND.withId(id)));
    }

    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public List<Product> searchProducts(String name, String brand, String category) {
        List<Product> products = new ArrayList<>();
        if (name != null) {
            products.addAll(productRepository.findByName(name));
        }
        if (brand != null) {
            products.addAll(productRepository.findByBrand(brand));
        }
        if (category != null) {
            products.addAll(productRepository.findByCategory(category));
        }
        return products;
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public Product update(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        updateBaseFields(existingProduct, product);

        return productRepository.save(existingProduct);
    }

    protected void updateBaseFields(Product existing, Product source) {
        existing.setName(source.getName());
        existing.setBrand(source.getBrand());
        existing.setDescription(source.getDescription());
        existing.setCategory(source.getCategory());
        existing.setPurchasePrice(source.getPurchasePrice());
        existing.setSalePrice(source.getSalePrice());
        existing.setOldSalePrice(source.getOldSalePrice());
        existing.setInStock(source.isInStock());
        existing.setProductType(source.getProductType());
        existing.setGender(source.getGender());
        existing.setVolume(source.getVolume());
    }

    @Transactional
    public Product patchUpdate(Long id, Map<String, Object> updates) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        updates.forEach((key, value) -> {
            if (value == null) {
                return; // пропускаем null значения
            }

            switch (key) {
                case "name":
                    existingProduct.setName((String) value);
                    break;
                case "brand":
                    existingProduct.setBrand((String) value);
                    break;
                case "description":
                    existingProduct.setDescription((String) value);
                    break;
                case "category":
                    existingProduct.setCategory((String) value);
                    break;
                case "salePrice":
                    existingProduct.setSalePrice(((Number) value).doubleValue());
                    break;
                case "oldSalePrice":
                    existingProduct.setOldSalePrice(((Number) value).doubleValue());
                    break;
                case "inStock":
                    existingProduct.setInStock((Boolean) value);
                    break;
                case "productType":
                    existingProduct.setProductType((String) value);
                    break;
                case "gender":
                    existingProduct.setGender(Gender.valueOf((String) value));
                    break;
                case "volume":
                    existingProduct.setVolume(((Number) value).doubleValue());
                    break;
                default:
                    break;
            }
        });

        return existingProduct;
    }


}