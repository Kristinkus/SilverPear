package com.example.silverpear.controller;

import com.example.silverpear.api.OrderApi;
import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.BulkOrderRequest;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.security.AppUserDetails;
import com.example.silverpear.security.AuthPrincipal;
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
    private final AuthPrincipal authPrincipal;

    private void requireOrderOwnerOrAdmin(Order order) {
        AppUserDetails me = authPrincipal.currentUser();
        if (!me.isAdmin() && !order.getUser().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к заказу");
        }
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getAllOrders() {
        authPrincipal.requireAdmin();
        List<Order> orders = orderService.findAllOrdersWithItemsAndProducts();
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<Object> createOrderWithTransaction(Long userId, OrderRequest request) {
        authPrincipal.requireSelfOrAdmin(userId);
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
            requireOrderOwnerOrAdmin(order);
            OrderForUserDto dto = orderForUserMapper.toDto(order);
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteOrder(Long orderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            requireOrderOwnerOrAdmin(order);
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<OrderForUserDto> updateOrder(Long orderId, OrderRequest request) {
        authPrincipal.requireAdmin();
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
        authPrincipal.requireSelfOrAdmin(userId);
        if (!orderService.orderBelongsToUser(orderId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }
        orderService.updateOrderStatus(orderId, status);
        Order hydratedOrder = orderService.findOrderByIdWithUserAndItemsAndProducts(orderId);
        return ResponseEntity.ok(orderForUserMapper.toDto(hydratedOrder));
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByStatus(OrderStatus status) {
        authPrincipal.requireAdmin();
        List<Order> order = orderService.findByStatus(status);
        return ResponseEntity.ok(orderForUserMapper.toDtoList(order));
    }

    @Override
    public ResponseEntity<Page<OrderForUserDto>> getAllOrdersPageable(Pageable pageable) {
        authPrincipal.requireAdmin();
        Page<Order> ordersPage = orderService.getOrdersPage(pageable);
        Page<OrderForUserDto> dtoPage = ordersPage.map(orderForUserMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByFilters(String brand, Double minAmount) {
        authPrincipal.requireAdmin();
        List<Order> orders = orderService.getOrdersByFilters(brand, minAmount);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> getOrdersByFiltersNative(String brand, Double minAmount) {
        authPrincipal.requireAdmin();
        List<Order> orders = orderService.getOrdersByFiltersNative(brand, minAmount);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<OrderForUserDto>> bulkCreateOrders(BulkOrderRequest request) {
        authPrincipal.requireAdmin();
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
        authPrincipal.requireAdmin();
        List<Order> orders = orderService.createOrderBulkWithoutTransaction(request);
        List<OrderForUserDto> dtos = orders.stream()
                .map(orderForUserMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

}
