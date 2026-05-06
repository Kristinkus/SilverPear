package com.example.silverpear.product.productdto;

import lombok.Data;

@Data
public class ProductSimpleDto {
    private Long id;
    private String name;
    private String brand;
    private String category;
    private double salePrice;
    private int stockQuantity;
    private String imageUrl;
}
