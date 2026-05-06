package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Schema(name = "GiftCardOrderRequest")
@Data
public class GiftCardOrderRequest {

    @NotBlank(message = "Выберите дизайн карты")
    @Pattern(regexp = "^card-[1-6]$", message = "Неизвестный дизайн карты")
    private String designId;

    @NotNull(message = "Укажите сумму")
    @DecimalMin(value = "1.0", message = "Минимальная сумма — 1 BYN")
    @DecimalMax(value = "2000.0", message = "Максимальная сумма — 2000 BYN")
    private BigDecimal amount;

    @NotBlank(message = "Укажите телефон получателя")
    @Pattern(regexp = "^\\+?[\\d\\s\\-]{10,22}$", message = "Некорректный номер получателя")
    private String recipientPhone;
}
