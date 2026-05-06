package com.example.silverpear.product.productdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftCardOrderResponse {

    private Long id;
    private String designId;
    private BigDecimal amount;
    private String recipientPhone;
    private Instant createdAt;
}
