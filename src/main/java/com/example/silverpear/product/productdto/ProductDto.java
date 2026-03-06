package com.example.silverpear.product.productdto;

import com.example.silverpear.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private String category;
    private double salePrice;
    private double oldSalePrice;
    private boolean inStock;
    private String productType;
    private Gender gender;
    private double volume;
}
