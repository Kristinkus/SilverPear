package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productdto.ProductDto;

//import java.sql.Array;
import java.util.List;
import java.util.ArrayList;


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
        return new ProductDto();
    }

    protected void fillBaseFields(Product product, ProductDto dto) {
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setSalePrice(product.getSalePrice());
        dto.setOldSalePrice(product.getOldSalePrice());
        dto.setInStock(product.isInStock());
        dto.setProductType(product.getProductType());
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
        product.setOldSalePrice(dto.getOldSalePrice());
        product.setInStock(dto.isInStock());
        product.setProductType(dto.getProductType());
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



}