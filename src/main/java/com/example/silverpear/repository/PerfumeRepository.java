package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.product.productdto.PerfumeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

}
