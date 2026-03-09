package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productdto.ProductDto;

import java.util.List;
import java.util.ArrayList;


import com.example.silverpear.product.productdto.ProductSimpleDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = createDto(product);

        fillBaseFields(product, dto);

        return dto;
    }

    protected ProductDto createDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDto();
    }

    protected void fillBaseFields(Product product, ProductDto dto) {
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setSalePrice(product.getSalePrice());

        dto.setInStock(product.isInStock());
        dto.setProductType(product.getType());
        dto.setGender(product.getGender());
        dto.setVolume(product.getVolume());
    }

    protected void fillBaseFieldsToEntity(Product product, ProductDto dto) {
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setSalePrice(dto.getSalePrice());

        product.setInStock(dto.isInStock());
        product.setType(dto.getProductType());
        product.setGender(dto.getGender());
        product.setVolume(dto.getVolume());
    }

    public Product toEntity(ProductDto dto) {
        Product product = new Product();
        fillBaseFieldsToEntity(product, dto);
        return  product;
    }

    public List<ProductDto> toDtoList(List<Product> products) {
        List<ProductDto> productsListDto = new ArrayList<>();
        for (Product product : products) {
            productsListDto.add(toDto(product));
        }
        return productsListDto;
    }

    public List<Product> toEntityList(List<ProductDto> productDtos) {
        List<Product> productsList = new ArrayList<>();
        if (productDtos != null) {
            for (ProductDto dto : productDtos) {
                productsList.add(toEntity(dto));
            }
        }
        return productsList;
    }

    public ProductSimpleDto toSimpleDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductSimpleDto dto = new ProductSimpleDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        return dto;
    }


}