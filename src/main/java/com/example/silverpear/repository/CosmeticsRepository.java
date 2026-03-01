package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Cosmetics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CosmeticsRepository extends JpaRepository<Cosmetics, Long> {

}
