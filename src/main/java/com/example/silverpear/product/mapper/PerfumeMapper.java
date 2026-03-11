package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.Perfume;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productdto.PerfumeDto;
import com.example.silverpear.product.productdto.ProductDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class PerfumeMapper extends ProductMapper {

    @Override
    protected ProductDto createDto(Product product) {
        return new PerfumeDto();
    }

    @Override
    public PerfumeDto toDto(Product product) {
        if (!(product instanceof Perfume)) {
            return (PerfumeDto) super.toDto(product);
        }

        Perfume perfume = (Perfume) product;
        PerfumeDto dto = new PerfumeDto();
        fillBaseFields(perfume, dto);

        dto.setTopNotes(perfume.getTopNotes());
        dto.setMiddleNotes(perfume.getMiddleNotes());
        dto.setBaseNotes(perfume.getBaseNotes());

        return dto;
    }

    public Perfume toEntity(PerfumeDto dto) {
        if (dto == null) {
            return null;
        }

        Perfume perfume = new Perfume();
        fillBaseFieldsToEntity(perfume, dto);

        perfume.setTopNotes(dto.getTopNotes());
        perfume.setMiddleNotes(dto.getMiddleNotes());
        perfume.setBaseNotes(dto.getBaseNotes());

        return perfume;
    }

    public List<PerfumeDto> toPerfumeDtoList(List<Perfume> perfumes) {
        List<PerfumeDto> list = new ArrayList<>();
        if (perfumes != null) {
            for (Perfume p : perfumes) {
                list.add(toDto(p));
            }
        }
        return list;
    }
}