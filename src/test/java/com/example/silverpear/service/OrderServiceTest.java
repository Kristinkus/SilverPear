package com.example.silverpear.service;

import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CacheService cacheService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userRepository, productRepository, cacheService, null);
    }

    @Test
    void createOrderWithTransaction_buildsBulkAndReturnsFirstOrder() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        Product product = new Product();
        product.setId(1L);
        product.setSalePrice(25.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequest request = new OrderRequest();
        request.setProductIds(List.of(1L));
        request.setQuantities(List.of(2));

        Order actual = orderService.createOrderWithTransaction(10L, request);

        assertSame(user, actual.getUser());
        assertEquals(50.0, actual.getTotalAmount(), 0.001);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderWithTransaction_usesInjectedSelfProxyWhenPresent() {
        OrderService selfProxy = mock(OrderService.class);
        OrderService service = new OrderService(orderRepository, userRepository, productRepository, cacheService, selfProxy);

        Order expected = new Order();
        when(selfProxy.createOrderBulkTransactional(any())).thenReturn(List.of(expected));

        OrderRequest request = new OrderRequest();
        request.setProductIds(List.of(1L));
        request.setQuantities(List.of(1));

        Order actual = service.createOrderWithTransaction(1L, request);

        assertSame(expected, actual);
        verify(selfProxy).createOrderBulkTransactional(any());
    }

    @Test
    void findAllOrdersWithItemsAndProducts_cachedAndRepository() {
        List<Order> cached = List.of(new Order());
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.findAllOrdersWithItemsAndProducts());

        List<Order> repo = List.of(new Order(), new Order());
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findAllOrdersWithItemsAndProducts()).thenReturn(repo);
        assertSame(repo, orderService.findAllOrdersWithItemsAndProducts());
        verify(cacheService).put(any(), eq(repo));
    }

    @Test
    void deleteOrder_success() {
        orderService.deleteOrder(1L);
        verify(orderRepository).deleteById(1L);
        verify(cacheService).evict(any());
        verify(cacheService).evictByPattern("Order:findAll");
        verify(cacheService).evictByPattern("Order:findByStatus");
    }

    @Test
    void findOrderById_cachedRepoAndNotFound() {
        Order cached = new Order();
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.findOrderById(1L));

        Order repo = new Order();
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(repo));
        assertSame(repo, orderService.findOrderById(2L));
        verify(cacheService).put(any(), eq(repo));

        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findById(3L)).thenReturn(Optional.empty());
        long id = 3L;
        assertThrows(RuntimeException.class, () -> orderService.findOrderById(id));
    }

    @Test
    void updateOrder_successAndNotFound() {
        Order existing = new Order();
        Product product = new Product();
        product.setSalePrice(10.0);
        when(cacheService.get(any())).thenReturn(existing);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(orderRepository.save(existing)).thenReturn(existing);

        OrderRequest request = new OrderRequest();
        request.setProductIds(List.of(5L));
        request.setQuantities(List.of(2));
        Order updated = orderService.updateOrder(11L, request);
        assertEquals(20.0, updated.getTotalAmount());
        verify(cacheService).put(any(), eq(updated));

        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());
        long orderId = 100L;
        assertThrows(RuntimeException.class, () -> orderService.updateOrder(orderId, request));

        Order existing2 = new Order();
        when(cacheService.get(any())).thenReturn(existing2);
        OrderRequest request2 = new OrderRequest();
        request2.setProductIds(List.of(404L));
        request2.setQuantities(List.of(1));
        when(productRepository.findById(404L)).thenReturn(Optional.empty());
        long orderId2 = 12L;
        assertThrows(RuntimeException.class, () -> orderService.updateOrder(orderId2, request2));
    }

    @Test
    void updateOrderStatus_success() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        when(cacheService.get(any())).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        Order updated = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void findByStatus_cachedAndRepo() {
        List<Order> cached = List.of(new Order());
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.findByStatus(OrderStatus.NEW));

        List<Order> repo = List.of(new Order());
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findOrderByStatus(OrderStatus.NEW)).thenReturn(repo);
        assertSame(repo, orderService.findByStatus(OrderStatus.NEW));
    }

    @Test
    void getOrdersPage_cachedAndRepo() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
        Page<Order> cached = new PageImpl<>(List.of(new Order()));
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.getOrdersPage(pageable));

        Page<Order> repo = new PageImpl<>(List.of(new Order(), new Order()));
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findAll(pageable)).thenReturn(repo);
        assertSame(repo, orderService.getOrdersPage(pageable));
    }

    @Test
    void getOrdersByFilters_cachedAndRepo() {
        List<Order> cached = List.of(new Order());
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.getOrdersByFilters("B", 10.0));

        List<Order> repo = List.of(new Order());
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findOrdersByBrandAndStatusAndMinAmount("B", 10.0)).thenReturn(repo);
        assertSame(repo, orderService.getOrdersByFilters("B", 10.0));
    }

    @Test
    void getOrdersByFiltersNative_cachedAndRepo() {
        List<Order> cached = List.of(new Order());
        when(cacheService.get(any())).thenReturn(cached);
        assertSame(cached, orderService.getOrdersByFiltersNative("B", 10.0));

        List<Order> repo = List.of(new Order());
        when(cacheService.get(any())).thenReturn(null);
        when(orderRepository.findOrdersByBrandAndStatusAndMinAmountNative("B", 10.0)).thenReturn(repo);
        assertSame(repo, orderService.getOrdersByFiltersNative("B", 10.0));
    }

    @Test
    void createOrderBulkWithoutTransaction_whenProductMissing_throws() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest();
        request.setProductIds(List.of(999L));
        request.setQuantities(List.of(1));

        var bulk = bulkRequest(1L, List.of(request));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderBulkWithoutTransaction(bulk));
        assertEquals("Some products not found - transaction will rollback!", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    private static com.example.silverpear.product.productdto.BulkOrderRequest bulkRequest(
            Long userId, List<OrderRequest> orders) {
        com.example.silverpear.product.productdto.BulkOrderRequest request =
                new com.example.silverpear.product.productdto.BulkOrderRequest();
        request.setUserId(userId);
        request.setOrders(orders);
        return request;
    }
}
