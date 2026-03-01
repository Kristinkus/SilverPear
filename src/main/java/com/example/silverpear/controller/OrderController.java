package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create-with-items")
    public ResponseEntity<?> createOrderWithItems(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithItems(
                    userId,
                    request.getProductIds(),
                    request.getQuantities()
            );
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании заказа: " + e.getMessage());
        }
    }

    // Эндпоинт для демонстрации проблемы N+1
    @GetMapping("/demonstrate-nplus1/{userId}")
    public ResponseEntity<String> demonstrateNPlusOne(@PathVariable Long userId) {
        orderService.demonstrateNPlusOneProblem(userId);
        return ResponseEntity.ok("Проверьте консоль для демонстрации проблемы N+1");
    }

    // Эндпоинт для демонстрации решения с @EntityGraph
    @GetMapping("/demonstrate-solution/{userId}")
    public ResponseEntity<String> demonstrateSolution(@PathVariable Long userId) {
        orderService.demonstrateSolutionWithEntityGraph(userId);
        return ResponseEntity.ok("Проверьте консоль для демонстрации решения с @EntityGraph");
    }

    // Эндпоинт для демонстрации частичного сохранения без @Transactional
    @PostMapping("/create-without-transaction")
    public ResponseEntity<?> createOrderWithoutTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrderWithoutTransaction(
                    userId,
                    request.getProductIds(),
                    request.getQuantities()
            );
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка, но заказ мог быть частично сохранен: " + e.getMessage());
        }
    }
}