package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "AdminUserListDto", description = "Пользователь для списка в админке")
public class AdminUserListDto {

    @Schema(description = "Идентификатор пользователя")
    private Long id;

    @Schema(description = "Логин (телефон)")
    private String login;

    @Schema(description = "Имя")
    private String name;

    @Schema(description = "Фамилия")
    private String surname;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "Телефон")
    private String phone;

    @Schema(description = "Маска пароля для UI")
    private String passwordMasked;

    @Schema(description = "Заказы пользователя")
    private List<AdminUserOrderSummaryDto> orders = new ArrayList<>();
}
