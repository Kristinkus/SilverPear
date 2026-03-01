package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Cosmetics;
//import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.List;

@Repository
public interface CosmeticsRepository extends JpaRepository<Cosmetics, Long> {
    //List<Cosmetics> findAllCosmetics();
}
