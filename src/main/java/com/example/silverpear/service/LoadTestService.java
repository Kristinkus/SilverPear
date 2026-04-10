package com.example.silverpear.service;

import com.example.silverpear.product.entity.Product;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class LoadTestService {

    private static final String LOAD_TEST_BRAND = "LOAD_TEST_BRAND";
    private static final String LOAD_TEST_CATEGORY = "LOAD_TEST_CATEGORY";
    private static final String LOAD_TEST_TYPE = "LOAD_TEST_TYPE";

    private final ProductService productService;

    public LoadTestService(ProductService productService) {
        this.productService = productService;
    }

    public Product createLoadTestProduct() {
        Product product = new Product();
        product.setName("LoadTest Product " + System.nanoTime());
        product.setBrand(LOAD_TEST_BRAND);
        product.setCategory(LOAD_TEST_CATEGORY);
        product.setDescription("Generated for load testing");
        product.setType(LOAD_TEST_TYPE);
        product.setSalePrice(ThreadLocalRandom.current().nextDouble(10.0, 1000.0));
        product.setInStock(true);
        product.setVolume(ThreadLocalRandom.current().nextDouble(10.0, 200.0));
        return productService.create(product);
    }
}
