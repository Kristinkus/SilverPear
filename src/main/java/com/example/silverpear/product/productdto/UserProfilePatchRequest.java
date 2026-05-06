package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(name = "UserProfilePatchRequest", description = "Смена фамилии, имени и отчества без смены пароля")
@Data
public class UserProfilePatchRequest {

    @Schema(description = "Фамилия", example = "Иванова")
    @NotBlank(message = "Укажите фамилию")
    @Size(min = 2, max = 30, message = "Фамилия: от 2 до 30 символов")
    private String surname;

    @Schema(description = "Имя", example = "Мария")
    @NotBlank(message = "Укажите имя")
    @Size(min = 2, max = 30, message = "Имя: от 2 до 30 символов")
    private String name;

    @Schema(description = "Отчество", example = "Сергеевна")
    @NotBlank(message = "Укажите отчество")
    @Size(min = 2, max = 30, message = "Отчество: от 2 до 30 символов")
    private String patronymic;
}
