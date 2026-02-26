package com.example.silverpear.product.productdto;

import com.example.silverpear.product.entity.Product;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(Product product) {
        return new UserDto(product);
    }

    public Product toEntity(UserDto userDto) {

        Product product = new Product();
        product.setId(userDto.id);
        product.setName(userDto.name);
        product.setBrand(userDto.brand);
        product.setCategory(userDto.category);
        return product;
    }

    public List<UserDto> toDtoList(List<Product> products) {

        List<UserDto> dtos = new ArrayList<>();
        products.forEach(product -> dtos.add(new UserDto(product)));  // лямбда с forEach
        return dtos;
    }

    public List<Product> toEntityList(List<UserDto> userDtos) {

        List<Product> products = new ArrayList<>();
        for (UserDto dto : userDtos) {  // обычный цикл for-each
            products.add(toEntity(dto));
        }
        return products;
    }

    public void updateEntityFromDto(UserDto userDto, Product product) {
        if (userDto == null || product == null) {
            return;
        }

        product.setId(userDto.id);
        product.setName(userDto.name);
        product.setBrand(userDto.brand);
        product.setCategory(userDto.category);
    }
}