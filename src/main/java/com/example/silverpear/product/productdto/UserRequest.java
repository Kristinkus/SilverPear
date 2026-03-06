package com.example.silverpear.product.productdto;

import lombok.Data;

@Data
public class UserRequest {
    private String login;
    private String password;
    private String name;
    private String surname;
    private String email;
    private String phone;
}