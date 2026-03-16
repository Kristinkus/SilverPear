package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.mapper.OrderForUserMapper;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private static final String CACHE_ENTITY_ORDER = "Order";
    private static final String CACHE_KEY_FIND_ALL = CACHE_ENTITY_ORDER + ":findAll";
    private static final String CACHE_KEY_FIND_BY_STATUS = CACHE_ENTITY_ORDER + ":findByStatus";
    private static final String CACHE_KEY_FIND_BY_ID = CACHE_ENTITY_ORDER + ":findById";
    private static final String CACHE_METHOD_FIND_BY_ID = "findById";

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderForUserMapper orderForUserMapper;
    private final CacheService cacheService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderForUserMapper orderForUserMapper,
                        CacheService cacheService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderForUserMapper = orderForUserMapper;
        this.cacheService = cacheService;
    }

    public Order createOrderWithoutTransaction(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setUser(user);

        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;

        for (int i = 0; i < request.getProductIds().size(); i++) {
            Long productId = request.getProductIds().get(i);
            Integer quantity = request.getQuantities().get(i);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Some products not found - order already saved!"));

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);

            savedOrder.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }

        savedOrder.setTotalAmount(totalAmount);
        Order created = orderRepository.save(savedOrder);

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache invalidated after order creation");

        return created;
    }

    @Transactional
    public Order createOrderWithTransaction(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Product> products = new ArrayList<>();
        for (Long productId : request.getProductIds()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Some products not found - transaction will rollback!"));
            products.add(product);
        }

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setUser(user);

        double totalAmount = 0.0;

        for (int i = 0; i < request.getProductIds().size(); i++) {
            Product product = products.get(i);
            Integer quantity = request.getQuantities().get(i);

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(order);

            order.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }

        order.setTotalAmount(totalAmount);
        Order created = orderRepository.save(order);

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache invalidated after order creation with transaction");

        return created;
    }

    public List<Order> findAllOrdersWithoutOptimization() {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "findAllWithoutOptimization", "", 0, 0, "", "");

        List<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Orders retrieved from cache");
            return cached;
        }

        log.info("Orders not in cache");
        List<Order> orders = orderRepository.findAllOrdersWithoutOptimization();
        cacheService.put(key, orders);
        log.info("Orders saved to cache");

        return orders;
    }

    public List<Order> findAllOrdersWithItemsAndProducts() {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "findAllOrdersWithItemsAndProducts", "", 0, 0, "", "");

        List<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Orders with items retrieved from cache");
            return cached;
        }

        log.info("Orders with items not in cache");
        List<Order> orders = orderRepository.findAllOrdersWithItemsAndProducts();
        cacheService.put(key, orders);
        log.info("Orders with items saved to cache");

        return orders;
    }

    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);

        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, CACHE_METHOD_FIND_BY_ID, "id=" + orderId, 0, 0, "", "");
        cacheService.evict(key);
        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache invalidated after order deletion: {}", orderId);
    }

    public Order findOrderById(Long orderId) {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, CACHE_METHOD_FIND_BY_ID, "id=" + orderId, 0, 0, "", "");

        Order cached = cacheService.get(key);
        if (cached != null) {
            log.info("Order retrieved from cache: {}", orderId);
            return cached;
        }

        log.info("Order not in cache: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.ORDER_NOT_FOUND.withId(orderId)));

        cacheService.put(key, order);
        log.info("Order saved to cache: {}", orderId);

        return order;
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderRequest request) {
        Order existingOrder = findOrderById(orderId);
        existingOrder.getOrderItems().clear();

        List<Product> products = new ArrayList<>();
        for (Long productId : request.getProductIds()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            products.add(product);
        }

        double totalAmount = 0.0;
        for (int i = 0; i < request.getProductIds().size(); i++) {
            Product product = products.get(i);
            Integer quantity = request.getQuantities().get(i);

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(existingOrder);

            existingOrder.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }
        existingOrder.setTotalAmount(totalAmount);

        Order updated = orderRepository.save(existingOrder);

        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, CACHE_METHOD_FIND_BY_ID, "id=" + orderId, 0, 0, "", "");
        cacheService.put(key, updated);
        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache updated after order update: {}", orderId);

        return updated;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = findOrderById(orderId);
        order.setStatus(status);

        Order updated = orderRepository.save(order);

        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, CACHE_METHOD_FIND_BY_ID, "id=" + orderId, 0, 0, "", "");
        cacheService.put(key, updated);
        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache updated after status change: {}", orderId);

        return updated;
    }

    public List<Order> findByStatus(OrderStatus status) {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "findByStatus", "status=" + status, 0, 0, "", "");

        List<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Orders by status {} retrieved from cache", status);
            return cached;
        }

        log.info("Orders by status {} not in cache", status);
        List<Order> orders = orderRepository.findOrderByStatus(status);
        cacheService.put(key, orders);
        log.info("Orders by status {} saved to cache", status);

        return orders;
    }

    public Page<OrderForUserDto> getOrdersPage(int page, int size, String sortBy) {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "getOrdersPage", "", page, size, sortBy, "desc");

        Page<OrderForUserDto> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Orders page {} retrieved from cache", page);
            return cached;
        }

        log.info("Orders page {} not in cache", page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderForUserDto> dtoPage = orders.map(orderForUserMapper::toDto);

        cacheService.put(key, dtoPage);
        log.info("Orders page {} saved to cache", page);

        return dtoPage;
    }

    public Page<Order> getOrdersPage(Pageable pageable) {
        CacheKey key = new CacheKey(
                CACHE_ENTITY_ORDER,
                "getOrdersPage",
                "",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().iterator().next().getProperty(),
                pageable.getSort().iterator().next().getDirection().name().toLowerCase()
        );

        Page<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Orders page {} retrieved from cache", pageable.getPageNumber());
            return cached;
        }

        log.info("Orders page {} not in cache", pageable.getPageNumber());
        Page<Order> orders = orderRepository.findAll(pageable);
        cacheService.put(key, orders);
        log.info("Orders page {} saved to cache", pageable.getPageNumber());

        return orders;
    }

    public List<Order> getOrdersByFilters(String brand, Double minAmount) {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "getOrdersByFilters",
                "brand=" + brand + "|minAmount=" + minAmount,
                0, 0, "", "");

        List<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Filtered orders retrieved from cache");
            return cached;
        }

        log.info("Filtered orders not in cache");
        List<Order> orders = orderRepository.findOrdersByBrandAndStatusAndMinAmount(brand, minAmount);
        cacheService.put(key, orders);
        log.info("Filtered orders saved to cache");

        return orders;
    }

    public List<Order> getOrdersByFiltersNative(String brand, Double minAmount) {
        CacheKey key = new CacheKey(CACHE_ENTITY_ORDER, "getOrdersByFiltersNative",
                "brand=" + brand + "|minAmount=" + minAmount,
                0, 0, "", "");

        List<Order> cached = cacheService.get(key);
        if (cached != null) {
            log.info("Filtered orders (native) retrieved from cache");
            return cached;
        }

        log.info("Filtered orders (native) not in cache");
        List<Order> orders = orderRepository.findOrdersByBrandAndStatusAndMinAmountNative(brand, minAmount);
        cacheService.put(key, orders);
        log.info("Filtered orders (native) saved to cache");

        return orders;
    }
}