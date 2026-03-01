package com.example.silverpear.service;


import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.repository.CosmeticsRepository;
import com.example.silverpear.repository.ProductRepository;
//import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CosmeticsService extends ProductService {

    @Autowired
    private CosmeticsRepository cosmeticsRepository;

    public CosmeticsService(ProductRepository productRepository) {
        super(productRepository);
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

    //update

    public Cosmetics updateCosmetics(Long id, Cosmetics cosmetics) {
        Cosmetics existingCosmetics = cosmeticsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cosmetics not found with id: " + id));

        super.updateBaseFields(existingCosmetics, cosmetics);

        existingCosmetics.setPrescription(cosmetics.getPrescription());
        existingCosmetics.setSkinType(cosmetics.getSkinType());
        existingCosmetics.setFinish(cosmetics.getFinish());

        return cosmeticsRepository.save(existingCosmetics);
    }

}
