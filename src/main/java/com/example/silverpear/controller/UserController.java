package com.example.silverpear.controller;

import com.example.silverpear.api.UserApi;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.AdminUserListDto;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.product.productdto.UserProfilePatchRequest;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import com.example.silverpear.security.AuthPrincipal;
import com.example.silverpear.service.OrderService;
import com.example.silverpear.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final OrderService orderService;
    private final OrderForUserMapper orderForUserMapper;
    private final AuthPrincipal authPrincipal;

    @Override
    public ResponseEntity<List<AdminUserListDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersForAdmin());
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        authPrincipal.requireSelfOrAdmin(id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserWithOrdersDto> getUserWithOrders(Long id) {
        authPrincipal.requireSelfOrAdmin(id);
        return ResponseEntity.ok(userService.getUserWithOrders(id));
    }

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(Long id, UserRequest request) {
        authPrincipal.requireSelfOrAdmin(id);
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<UserResponse> patchProfile(Long id, UserProfilePatchRequest request) {
        authPrincipal.requireSelfOrAdmin(id);
        return ResponseEntity.ok(userService.patchProfile(id, request));
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OrderForUserDto> createOrderForUser(Long userId, OrderRequest request) {
        authPrincipal.requireSelfOrAdmin(userId);
        Order order = orderService.createOrderWithTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderForUserMapper.toDto(order));
    }
}
