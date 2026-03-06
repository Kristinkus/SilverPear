package com.example.silverpear.product.productdto;

import lombok.Data;
import java.util.List;

@Data
public class UserWithOrdersDto {
    private Long id;
    private String login;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private List<OrderForUserDto> orders; // список заказов без обратной ссылки
}