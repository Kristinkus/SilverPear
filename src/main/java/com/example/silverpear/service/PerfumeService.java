package com.example.silverpear.service;


import com.example.silverpear.product.entity.Perfume;

import com.example.silverpear.repository.PerfumeRepository;
import com.example.silverpear.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfumeService extends ProductService {

    @Autowired
    private PerfumeRepository perfumeRepository;

    public PerfumeService(ProductRepository productRepository) {
        super(productRepository);
    }

    public Perfume create(Perfume perfume) {
        return perfumeRepository.save(perfume);
    }

    public List<Perfume> findAllPerfume() {
        return perfumeRepository.findAll();
    }

    public void delete(Perfume perfume) {
        perfumeRepository.delete(perfume);
    }

    public Perfume updatePerfume(Long id, Perfume perfume) {
        Perfume existingPerfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfume not found with id: " + id));

        super.updateBaseFields(existingPerfume, perfume);

        existingPerfume.setTopNotes(perfume.getTopNotes());
        existingPerfume.setMiddleNotes(perfume.getMiddleNotes());
        existingPerfume.setBaseNotes(perfume.getBaseNotes());

        return perfumeRepository.save(existingPerfume);
    }


}
