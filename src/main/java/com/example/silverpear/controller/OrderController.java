package com.example.silverpear.controller;

import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.errors.ErrorResponse;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/orders/demo")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/without-tx")
    public ResponseEntity<Object> createOrderWithoutTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithoutTransaction(userId, request);
            return ResponseEntity.ok(order);
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

    @PostMapping("/with-tx")
    public ResponseEntity<Object> createOrderWithTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithTransaction(userId, request);  // <-- передайте userId
            return ResponseEntity.ok(order);
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

}