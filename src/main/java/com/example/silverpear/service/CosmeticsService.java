package com.example.silverpear.service;


import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.repository.CosmeticsRepository;
import com.example.silverpear.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.example.silverpear.enums.ErrorMessages;
import java.util.List;

@Service

public class CosmeticsService extends ProductService {

    private final CosmeticsRepository cosmeticsRepository;
    private final CacheService cacheService;

    public CosmeticsService(ProductRepository productRepository,
                            CosmeticsRepository cosmeticsRepository,
                            CacheService cacheService) {
        super(productRepository, cacheService);
        this.cosmeticsRepository = cosmeticsRepository;
        this.cacheService = cacheService;
    }

    public Cosmetics create(Cosmetics cosmetics) {
        return cosmeticsRepository.save(cosmetics);
    }

    public List<Cosmetics> findAllCosmetics() {
        return cosmeticsRepository.findAll();
    }

    public void delete(Cosmetics cosmetics) {
        cosmeticsRepository.delete(cosmetics);
    }

    public Cosmetics updateCosmetics(Long id, Cosmetics cosmetics) {
        Cosmetics existingCosmetics = cosmeticsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.COSMETICS_NOT_FOUND.withId(id)));
        super.updateBaseFields(existingCosmetics, cosmetics);

        existingCosmetics.setPrescription(cosmetics.getPrescription());
        existingCosmetics.setSkinType(cosmetics.getSkinType());
        existingCosmetics.setFinish(cosmetics.getFinish());

        return cosmeticsRepository.save(existingCosmetics);
    }

}
