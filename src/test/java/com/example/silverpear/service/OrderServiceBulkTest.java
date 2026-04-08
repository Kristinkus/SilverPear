package com.example.silverpear.service;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.BulkOrderRequest;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceBulkTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderForUserMapper orderForUserMapper;
    @Mock
    private CacheService cacheService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                userRepository,
                productRepository,
                orderForUserMapper,
                cacheService
        );
    }

    @Test
    void createOrderBulkTransactional_success() {
        User user = new User();
        user.setId(1L);

        Product p1 = product(10L, 100.0);
        Product p2 = product(20L, 200.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(p2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Order> created = orderService.createOrderBulkTransactional(bulkRequest(1L, List.of(
                orderRequest(List.of(10L, 20L), List.of(1, 2)),
                orderRequest(List.of(20L), List.of(1))
        )));

        assertEquals(2, created.size());
        assertNotNull(created.getFirst().getOrderNumber());
        verify(cacheService).evictByPattern("Order:findAll");
        verify(cacheService).evictByPattern("Order:findByStatus");
    }

    @Test
    void createOrderBulkWithoutTransaction_whenSecondSaveFails_noCacheEvict() {
        User user = new User();
        user.setId(1L);

        Product p1 = product(10L, 100.0);
        Product p2 = product(20L, 200.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(p2));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new RuntimeException("DB failure on second save"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrderBulkWithoutTransaction(bulkRequest(1L, List.of(
                        orderRequest(List.of(10L), List.of(1)),
                        orderRequest(List.of(20L), List.of(2))
                )))
        );

        assertEquals("DB failure on second save", ex.getMessage());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cacheService, never()).evictByPattern(any());
    }

    @Test
    void createOrderBulkTransactional_whenProductNotFound_throwsAndStops() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrderBulkTransactional(bulkRequest(1L, List.of(
                        orderRequest(List.of(999L), List.of(1))
                )))
        );

        assertEquals("Some products not found - transaction will rollback!", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cacheService, never()).evictByPattern(any());
    }

    @Test
    void createOrderBulk_legacyMethod_delegatesToBulk() {
        User user = new User();
        user.setId(1L);
        Product p1 = product(10L, 150.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Order> created = orderService.createOrderBulk(bulkRequest(1L, List.of(
                orderRequest(List.of(10L), List.of(2))
        )));

        assertEquals(1, created.size());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        assertEquals(300.0, orderCaptor.getValue().getTotalAmount());
        verify(cacheService).evictByPattern(eq("Order:findAll"));
        verify(cacheService).evictByPattern(eq("Order:findByStatus"));
    }

    private static Product product(Long id, double price) {
        Product product = new Product();
        product.setId(id);
        product.setSalePrice(price);
        return product;
    }

    private static OrderRequest orderRequest(List<Long> productIds, List<Integer> quantities) {
        OrderRequest request = new OrderRequest();
        request.setProductIds(productIds);
        request.setQuantities(quantities);
        return request;
    }

    private static BulkOrderRequest bulkRequest(Long userId, List<OrderRequest> orders) {
        BulkOrderRequest request = new BulkOrderRequest();
        request.setUserId(userId);
        request.setOrders(orders);
        return request;
    }
}
