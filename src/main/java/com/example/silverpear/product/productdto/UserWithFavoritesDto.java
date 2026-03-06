package com.example.silverpear.product.productdto;

import lombok.Data;
import java.util.Set;

@Data
public class UserWithFavoritesDto {
    private Long id;
    private String login;
    private String name;
    private String surname;
    private Set<ProductSimpleDto> favorites;
}