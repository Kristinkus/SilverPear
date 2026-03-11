package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.productdto.OrderForUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderForUserMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderForUserDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        OrderForUserDto dto = new OrderForUserDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setOrderItems(order.getOrderItems().stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public List<OrderForUserDto> toDtoList(List<Order> orders) {
        List<OrderForUserDto> ordersListDto = new ArrayList<>();
        for (Order order : orders) {
            ordersListDto.add(toDto(order));
        }
        return ordersListDto;
    }
}