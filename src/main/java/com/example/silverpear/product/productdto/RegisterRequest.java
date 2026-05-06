package com.example.silverpear.product.productdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Регистрация: телефон + пароль + фамилия, имя, отчество (или устаревшее одно поле {@code fio}).
 */
@Schema(name = "RegisterRequest", description = "Регистрация по телефону, паролю и ФИО")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @Schema(description = "Телефон с кодом страны", example = "+375 29 676 77 36")
    private String phone;

    @Schema(description = "Устарело: если телефон не передан, но это похоже на номер — используется как телефон")
    private String login;

    @Schema(description = "Пароль", example = "password12")
    private String password;

    @Schema(description = "Устарело: ФИО одной строкой", example = "Иванова Мария Сергеевна")
    private String fio;

    @Schema(description = "Имя", example = "Мария")
    private String name;

    @Schema(description = "Фамилия", example = "Иванова")
    private String surname;

    @Schema(description = "Отчество", example = "Сергеевна")
    private String patronymic;

    @Schema(hidden = true)
    private String email;
}
