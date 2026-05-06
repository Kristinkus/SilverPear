package com.example.silverpear.product.productdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "AdminUserOrderSummaryDto", description = "Краткие данные заказа пользователя для админки")
public class AdminUserOrderSummaryDto {

    @Schema(description = "Идентификатор заказа")
    private Long id;

    @Schema(description = "Номер заказа")
    private String orderNumber;

    @Schema(description = "Статус заказа")
    private String status;

    @Schema(description = "Дата заказа")
    private LocalDateTime orderDate;

    @Schema(description = "Сумма заказа")
    private Double totalAmount;
}
