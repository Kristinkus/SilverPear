package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(name = "UserRequest", description = "Данные для создания и обновления пользователя")
@Data
public class UserRequest {

    @Schema(description = "Уникальный логин", example = "ivan")
    @NotBlank(message = "Логин обязателен")
    @Size(min = 2, max = 64, message = "Логин: от 2 до 64 символов")
    private String login;

    @Schema(description = "Пароль (не короче 8 символов)", example = "password12")
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 36, message = "Пароль: не менее 8 символов")
    private String password;

    @Schema(description = "Имя", example = "Иван")
    @NotBlank(message = "Имя обязательно")
    @Size(max = 30, message = "Имя: не более 30 символов")
    private String name;

    @Schema(description = "Фамилия", example = "Иванов")
    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 30, message = "Фамилия: не более 30 символов")
    private String surname;

    @Schema(description = "Отчество", example = "Иванович")
    @Size(max = 30, message = "Отчество: не более 30 символов")
    private String patronymic;

    @Schema(description = "Электронная почта (необязательно при регистрации по телефону)", example = "ivan@mail.ru")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Телефон", example = "+375 29 123 45 67")
    @Pattern(regexp = "^$|^\\+?[\\d\\s\\-]{10,22}$", message = "Некорректный формат телефонного номера")
    private String phone;
}
