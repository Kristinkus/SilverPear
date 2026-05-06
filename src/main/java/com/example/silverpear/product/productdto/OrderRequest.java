package com.example.silverpear.product.productdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class OrderRequest {
    /**
     * Ключи — строковые ID товаров (как в JSON из браузера), значения — количество.
     */
    @NotEmpty(message = "Список товаров не должен быть пустым")
    private Map<
            @NotBlank(message = "ID продукта не должен быть пустым") String,
            @NotNull(message = "Количество не должно быть null")
            @Positive(message = "Количество должно быть положительным") Integer> productQuantities;

    /**
     * Необязательно: сколько списать с подарочного баланса (не больше суммы заказа и доступного баланса).
     */
    private Double giftCardAmount;
}
