package com.example.silverpear.product.productdto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String login;
    private String name;
    private String surname;
}