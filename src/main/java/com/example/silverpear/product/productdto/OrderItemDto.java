package com.example.silverpear.product.productdto;


import lombok.Data;

@Data
public class OrderItemDto {
    private Long id;
    private Integer quantity;
    private Double priceAtTime;
    private ProductSimpleDto product;
}
