package com.example.silverpear.product.productdto;

import com.example.silverpear.product.entity.Product;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
public class UserDto {
    int id;
    String name;
    String brand;
    String category;
    double salePrice;

    public UserDto(Product product) {
        this.name = product.getName();
        this.brand = product.getBrand();
        this.category = product.getCategory();
        this.salePrice = product.getSalePrice();
    }
}
