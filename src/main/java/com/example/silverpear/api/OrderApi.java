package com.example.silverpear.api;

import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import java.util.List;

@RequestMapping("/api/orders")
@Tag(name = "Заказы", description = "Создание, поиск и изменение заказов")
public interface OrderApi {

    @GetMapping
    @Operation(summary = "Все заказы")
    ResponseEntity<List<OrderForUserDto>> getAllOrders();

    @PostMapping("/create")
    @Operation(summary = "Создать заказ (транзакция)")
    ResponseEntity<Object> createOrderWithTransaction(
            @RequestParam Long userId,
            @RequestBody OrderRequest request);

    @GetMapping("/{orderId}")
    @Operation(summary = "Заказ по id")
    ResponseEntity<OrderForUserDto> getOrderById(@PathVariable Long orderId);

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Удалить заказ")
    ResponseEntity<Void> deleteOrder(@PathVariable Long orderId);

    @PutMapping("/{orderId}")
    @Operation(summary = "Обновить заказ")
    ResponseEntity<OrderForUserDto> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderRequest request);

    @PatchMapping("/{userId}/user-orders/{orderId}")
    @Operation(summary = "Обновить статус заказа пользователя")
    ResponseEntity<OrderForUserDto> updateOrderStatus(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status);

    @GetMapping("/status")
    @Operation(summary = "Заказы по статусу")
    ResponseEntity<List<OrderForUserDto>> getOrdersByStatus(@RequestParam OrderStatus status);

    @GetMapping("/pageable")
    @Operation(summary = "Страница заказов")
    ResponseEntity<Page<OrderForUserDto>> getAllOrdersPageable(
            @PageableDefault(size = 2, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable);

    @GetMapping("/jpql")
    @Operation(summary = "Фильтр заказов (JPQL)")
    ResponseEntity<List<OrderForUserDto>> getOrdersByFilters(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") Double minAmount);

    @GetMapping("/native")
    @Operation(summary = "Фильтр заказов (native)")
    ResponseEntity<List<OrderForUserDto>> getOrdersByFiltersNative(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") Double minAmount);
}
