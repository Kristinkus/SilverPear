package com.example.silverpear.product.productdto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<Long> productIds;
    private List<Integer> quantities;
}