package com.example.silverpear.product.productdto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BulkOrderRequest {

    @NotNull(message = "ID не должен быть пустым")
    private Long userId;

    @NotEmpty(message = "Заказы не должны быть пустыми")
    @Valid
    private List<OrderRequest> orders;
}