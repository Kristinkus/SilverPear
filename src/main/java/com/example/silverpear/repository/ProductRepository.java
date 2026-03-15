package com.example.silverpear.repository;


import com.example.silverpear.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface ProductRepository extends JpaRepository<Product, Long> {


    public List<Product> findByName(String name);
    public List<Product> findByCategory(String category);
    public  List<Product> findByBrand(String brand);

    public List<Product> findAll();
    public Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.salePrice > :lowPrice AND p.salePrice < :highPrice")
    List<Product> findInRange(@Param("lowPrice") double lowPrice, @Param("highPrice") double highPrice);


    Page<Product> findBySalePrice(double lowPrice, double highPrice, Pageable pageable);

}