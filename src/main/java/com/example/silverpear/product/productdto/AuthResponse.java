package com.example.silverpear.product.productdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String login;
    private String name;
    private String surname;
    private String patronymic;
    private String role;
}
