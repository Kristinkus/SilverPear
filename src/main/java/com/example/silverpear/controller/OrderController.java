package com.example.silverpear.controller;

import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.errors.ErrorResponse;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/orders/demo")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/without-tx")
    public ResponseEntity<Object> createOrderWithoutTransaction(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithoutTransaction(request);
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
    public ResponseEntity<Object> createOrderWithTransaction(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithTransaction(request);
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