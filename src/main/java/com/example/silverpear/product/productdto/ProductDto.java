package com.example.silverpear.product.productdto;

import com.example.silverpear.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "Название обязательно")
    private String name;

    private String brand;
    private String description;
    private String category;


    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private double salePrice;
    private double oldSalePrice;
    private boolean inStock;

    private String productType;
    private Gender gender;
    private double volume;
}
