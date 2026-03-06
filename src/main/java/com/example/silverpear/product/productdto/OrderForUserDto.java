package com.example.silverpear.product.productdto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderForUserDto {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<OrderItemDto> orderItems;
}