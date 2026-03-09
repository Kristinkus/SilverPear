package com.example.silverpear.controller;

import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.errors.ErrorResponse;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders/demo")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderForUserMapper orderForUserMapper;

    @GetMapping("/without-nplus1")
    public ResponseEntity<List<OrderForUserDto>> getAllOrders() {
        List<Order> orders = orderService.findAllOrdersWithItemsAndProducts();
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/nplus1")
    public ResponseEntity<List<OrderForUserDto>> getAllOrdersNPlus1() {
        List<Order> orders = orderService.findAllOrdersWithoutOptimization();
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/without-transaction")
    public ResponseEntity<Object> createOrderWithoutTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithoutTransaction(userId, request);
            OrderForUserDto dto = orderForUserMapper.toDto(order);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                    ErrorMessages.UNEXPECTED_ERROR.name(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/with-transaction")
    public ResponseEntity<Object> createOrderWithTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithTransaction(userId, request);
            OrderForUserDto dto = orderForUserMapper.toDto(order);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                    ErrorMessages.UNEXPECTED_ERROR.name(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderForUserDto> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            OrderForUserDto dto = orderForUserMapper.toDto(order);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderForUserDto> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderRequest request) {
        try {
            Order updatedOrder = orderService.updateOrder(orderId, request);
            OrderForUserDto dto = orderForUserMapper.toDto(updatedOrder);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PatchMapping("/{userId}/orders/{orderId}")
    public ResponseEntity<OrderForUserDto> updateOrderStatus(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {


        Order order = orderService.findOrderById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }

        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderForUserMapper.toDto(updatedOrder));
    }

}
