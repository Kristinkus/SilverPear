package com.example.silverpear.service;


import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.repository.PerfumeRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.service.CacheService;
import org.springframework.stereotype.Component;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfumeService extends ProductService {

    private final PerfumeRepository perfumeRepository;
    private final CacheService cacheService;
    
    public PerfumeService(ProductRepository productRepository, PerfumeRepository perfumeRepository, CacheService cacheService) {
        super(productRepository, cacheService);
        this.perfumeRepository = perfumeRepository;
        this.cacheService = cacheService;
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
                .orElseThrow(() -> new RuntimeException(ErrorMessages.PERFUME_NOT_FOUND.withId(id)));
        super.updateBaseFields(existingPerfume, perfume);

        existingPerfume.setTopNotes(perfume.getTopNotes());
        existingPerfume.setMiddleNotes(perfume.getMiddleNotes());
        existingPerfume.setBaseNotes(perfume.getBaseNotes());

        return perfumeRepository.save(existingPerfume);
    }


}
