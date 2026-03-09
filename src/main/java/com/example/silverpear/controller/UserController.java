package com.example.silverpear.controller;

import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import com.example.silverpear.service.OrderService;
import com.example.silverpear.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private  final OrderService orderService;
    private final OrderForUserMapper orderForUserMapper;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<UserWithOrdersDto> getUserWithOrders(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserWithOrders(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrderForUserDto> createOrderForUser(
            @PathVariable Long userId,
            @RequestBody OrderRequest request) {

        Order order = orderService.createOrderWithTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderForUserMapper.toDto(order));
    }
}