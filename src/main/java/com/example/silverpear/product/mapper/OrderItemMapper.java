package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.productdto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final ProductMapper productMapper;

    public OrderItemDto toDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtTime(orderItem.getPriceAtTime());
        dto.setProduct(productMapper.toSimpleDto(orderItem.getProduct()));
        return dto;
    }
}