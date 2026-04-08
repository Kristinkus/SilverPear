package com.example.silverpear.product.productdto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty(message = "ID продуктов не должны быть пустыми")
    private List<@NotNull(message = "ID продукта не должен быть null") Long> productIds;

    @NotEmpty(message = "Количество продуктов не должно быть пустым")
    private List<
            @NotNull(message = "Количество не должно быть null")
            @Positive(message = "Количество должно быть положительным")
            Integer> quantities;

    @AssertTrue(message = "Количество productIds и quantities должно совпадать")
    public boolean isValidPairsSize() {
        return productIds != null && quantities != null && productIds.size() == quantities.size();
    }
}
