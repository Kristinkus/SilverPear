package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;

@Repository

public interface ProductRepository extends JpaRepository<Product, Long> {
    //CRUD

    public List<Product> findByName(String name);
    public List<Product> findByCategory(String category);
    //public List<Product> findInRange(double salePrice);
    public  List<Product> findByBrand(String brand);

}