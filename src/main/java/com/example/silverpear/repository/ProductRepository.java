package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface ProductRepository extends JpaRepository<Product, Long> {


    public List<Product> findByName(String name);
    public List<Product> findByCategory(String category);
    public  List<Product> findByBrand(String brand);

}