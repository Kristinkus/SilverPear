package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.Cosmetics;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productdto.CosmeticsDto;
import com.example.silverpear.product.productdto.ProductDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class CosmeticsMapper extends ProductMapper {

    @Override
    protected ProductDto createDto(Product product) {
        return new CosmeticsDto();
    }

    @Override
    public CosmeticsDto toDto(Product product) {
        if (!(product instanceof Cosmetics)) {
            return (CosmeticsDto) super.toDto(product);
        }

        Cosmetics cosmetics = (Cosmetics) product;
        CosmeticsDto dto = new CosmeticsDto();
        fillBaseFields(cosmetics, dto);

        dto.setPrescription(cosmetics.getPrescription());
        dto.setSkinType(cosmetics.getSkinType());
        dto.setFinish(cosmetics.getFinish());

        return dto;
    }

    public Cosmetics toEntity(CosmeticsDto dto) {
        if (dto == null) {
            return null;
        }


        Cosmetics cosmetics = new Cosmetics();
        fillBaseFieldsToEntity(cosmetics, dto);

        cosmetics.setPrescription(dto.getPrescription());
        cosmetics.setSkinType(dto.getSkinType());
        cosmetics.setFinish(dto.getFinish());

        return cosmetics;
    }

    public List<CosmeticsDto> toCosmeticsDtoList(List<Cosmetics> cosmetics) {
        List<CosmeticsDto> list = new ArrayList<>();
        if (cosmetics != null) {
            for (Cosmetics c : cosmetics) {
                list.add(toDto(c));
            }
        }
        return list;
    }
}