package com.example.silverpear.product.productDto;

import com.example.silverpear.product.entity.Product;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
public class UserDto {
    int id;
    String name;
    double price;
    String brand;
    String category;
    double salePrice;

    public UserDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.brand = product.getBrand();
        this.category = product.getCategory();
        this.salePrice = product.overprice();
    }

}
