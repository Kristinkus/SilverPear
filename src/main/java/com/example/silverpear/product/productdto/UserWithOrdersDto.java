package com.example.silverpear.product.productdto;

import lombok.Data;
import java.util.List;

@Data
public class UserWithOrdersDto {
    private Long id;
    private String login;
    private String name;
    private String surname;
    private String patronymic;
    private String email;
    private String phone;
    private Double giftBalance;
    private List<OrderForUserDto> orders;
}