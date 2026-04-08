package com.example.silverpear.controller;

import com.example.silverpear.api.OrderApi;
import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.BulkOrderRequest;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final OrderForUserMapper orderForUserMapper;

    @Override
    public ResponseEntity<List<OrderForUserDto>> getAllOrders() {
        List<Order> orders = orderService.findAllOrdersWithItemsAndProducts();
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<Object> createOrderWithTransaction(Long userId, OrderRequest request) {
        BulkOrderRequest bulkRequest = new BulkOrderRequest();
        bulkRequest.setUserId(userId);
        bulkRequest.setOrders(List.of(request));
        List<Order> orders = orderService.createOrderBulkTransactional(bulkRequest);
        Order order = orders.getFirst();
        OrderForUserDto dto = orderForUserMapper.toDto(order);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<OrderForUserDto> getOrderById(Long orderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            OrderForUserDto dto = orderForUserMapper.toDto(order);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteOrder(Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<OrderForUserDto> updateOrder(Long orderId, OrderRequest request) {
        try {
            Order updatedOrder = orderService.updateOrder(orderId, request);
            OrderForUserDto dto = orderForUserMapper.toDto(updatedOrder);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<OrderForUserDto> updateOrderStatus(Long userId, Long orderId, OrderStatus status) {
        Order order = orderService.findOrderById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderForUserMapper.toDto(updatedOrder));
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByStatus(OrderStatus status) {
        List<Order> order = orderService.findByStatus(status);
        return ResponseEntity.ok(orderForUserMapper.toDtoList(order));
    }

    @Override
    public ResponseEntity<Page<OrderForUserDto>> getAllOrdersPageable(Pageable pageable) {
        Page<Order> ordersPage = orderService.getOrdersPage(pageable);
        Page<OrderForUserDto> dtoPage = ordersPage.map(orderForUserMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByFilters(String brand, Double minAmount) {
        List<Order> orders = orderService.getOrdersByFilters(brand, minAmount);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByFiltersNative(String brand, Double minAmount) {
        List<Order> orders = orderService.getOrdersByFiltersNative(brand, minAmount);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> bulkCreateOrders(BulkOrderRequest request) {
        List<Order> orders = orderService.createOrderBulkTransactional(request);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> bulkCreateOrdersTransactional(BulkOrderRequest request) {
        return bulkCreateOrders(request);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> bulkCreateOrdersWithoutTransaction(BulkOrderRequest request) {
        List<Order> orders = orderService.createOrderBulkWithoutTransaction(request);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

}
