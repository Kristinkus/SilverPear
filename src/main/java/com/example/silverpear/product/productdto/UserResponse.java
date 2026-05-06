package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "UserResponse", description = "Пользователь без чувствительных полей")
@Data
public class UserResponse {

    @Schema(description = "Идентификатор")
    private Long id;

    @Schema(description = "Логин")
    private String login;

    @Schema(description = "Имя")
    private String name;

    @Schema(description = "Фамилия")
    private String surname;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "Роль")
    private String role;
}
