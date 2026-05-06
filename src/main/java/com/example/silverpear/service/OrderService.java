package com.example.silverpear.service;

import com.example.silverpear.cache.CacheKey;
import com.example.silverpear.enums.ErrorMessages;
import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.BulkOrderRequest;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private static final String CACHE_ENTITY_ORDER = "Order";
    private static final String CACHE_KEY_FIND_ALL = CACHE_ENTITY_ORDER + ":findAll";
    private static final String CACHE_KEY_FIND_BY_STATUS = CACHE_ENTITY_ORDER + ":findByStatus";
    private static final String CACHE_METHOD_FIND_BY_ID = "findById";
    private static final double FREE_DELIVERY_THRESHOLD = 55.0;
    private static final double DELIVERY_FEE = 7.0;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CacheService cacheService;
    private final OrderService self;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CacheService cacheService,
                        @Lazy OrderService self) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cacheService = cacheService;
        this.self = self != null ? self : this;
    }

    @Transactional
    public Order createOrderWithTransaction(Long userId, OrderRequest request) {
        BulkOrderRequest bulkOrderRequest = new BulkOrderRequest();
        bulkOrderRequest.setUserId(userId);
        bulkOrderRequest.setOrders(List.of(request));
        return self.createOrderBulkTransactional(bulkOrderRequest).getFirst();
    }

    public List<Order> findAllOrdersWithItemsAndProducts() {
        // Do not cache entity graphs with lazy associations:
        // detached proxies from cache can fail with "no session" during DTO mapping.
        return orderRepository.findAllOrdersWithItemsAndProducts();
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            return;
        }
        Order order = opt.get();
        refundGiftBalanceIfApplied(order);
        orderRepository.delete(order);

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

    @Transactional(readOnly = true)
    public Order findOrderByIdWithUserAndItemsAndProducts(Long orderId) {
        return orderRepository.findByIdWithUserAndItemsAndProducts(orderId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.ORDER_NOT_FOUND.withId(orderId)));
    }

    @Transactional(readOnly = true)
    public boolean orderBelongsToUser(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUser_Id(orderId, userId);
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderRequest request) {
        Order existingOrder = findOrderById(orderId);
        refundGiftBalanceIfApplied(existingOrder);
        existingOrder.getOrderItems().clear();

        double itemsTotal = 0.0;
        for (Map.Entry<String, Integer> entry : request.getProductQuantities().entrySet()) {
            long productId = parseProductIdKey(entry.getKey());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            Integer quantity = entry.getValue();

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(existingOrder);

            existingOrder.addOrderItem(item);
            itemsTotal += product.getSalePrice() * quantity;
        }
        double totalAmount = applyDeliveryFee(itemsTotal);
        existingOrder.setTotalAmount(totalAmount);
        existingOrder.setGiftCardAppliedAmount(null);

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
        Order order = orderRepository.findByIdWithUserAndItemsAndProducts(orderId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.ORDER_NOT_FOUND.withId(orderId)));
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


    @Transactional
    public List<Order> createOrderBulkTransactional(BulkOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        List<Order> result = request.getOrders().stream()
                .map(orderRequest -> buildOrder(user, orderRequest))
                .map(orderRepository::save)
                .toList();

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache invalidated after bulk order creation");
        return result;
    }

    public List<Order> createOrderBulkWithoutTransaction(BulkOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        List<Order> result = new ArrayList<>();
        for (OrderRequest orderRequest : request.getOrders()) {
            Order order = buildOrder(user, orderRequest);
            result.add(orderRepository.saveAndFlush(order));
        }

        cacheService.evictByPattern(CACHE_KEY_FIND_ALL);
        cacheService.evictByPattern(CACHE_KEY_FIND_BY_STATUS);
        log.info("Cache invalidated after bulk order creation");
        return result;
    }

    private Order buildOrder(User user, OrderRequest request) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setUser(user);

        double itemsTotal = request.getProductQuantities().entrySet().stream()
                .mapToDouble(entry -> {
                    long productId = parseProductIdKey(entry.getKey());
                    Product product = findProductOrThrow(productId);
                    Integer quantity = entry.getValue();
                    OrderItem item = new OrderItem();
                    item.setQuantity(quantity);
                    item.setPriceAtTime(product.getSalePrice());
                    item.setProduct(product);
                    item.setOrder(order);
                    order.addOrderItem(item);
                    return product.getSalePrice() * quantity;
                }).sum();
        double totalAmount = applyDeliveryFee(itemsTotal);

        order.setTotalAmount(totalAmount);

        Double giftReq = request.getGiftCardAmount();
        if (giftReq != null && giftReq > 1e-9) {
            BigDecimal balance = user.getGiftBalance() != null ? user.getGiftBalance() : BigDecimal.ZERO;
            BigDecimal grossBd = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal reqBd = BigDecimal.valueOf(giftReq).setScale(2, RoundingMode.HALF_UP);
            if (reqBd.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Сумма списания с подарочного баланса должна быть больше нуля");
            }
            BigDecimal maxApply = grossBd.min(balance);
            if (maxApply.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Недостаточно средств на подарочном балансе");
            }
            BigDecimal applyBd = reqBd.min(maxApply);
            user.setGiftBalance(balance.subtract(applyBd).setScale(2, RoundingMode.HALF_UP));
            userRepository.save(user);
            order.setGiftCardAppliedAmount(applyBd.doubleValue());
        } else {
            order.setGiftCardAppliedAmount(null);
        }

        return order;
    }

    private void refundGiftBalanceIfApplied(Order order) {
        Double applied = order.getGiftCardAppliedAmount();
        if (applied == null || applied <= 1e-9) {
            return;
        }
        User user = order.getUser();
        BigDecimal balance = user.getGiftBalance() != null ? user.getGiftBalance() : BigDecimal.ZERO;
        user.setGiftBalance(balance.add(BigDecimal.valueOf(applied)).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);
        order.setGiftCardAppliedAmount(null);
    }

    private Product findProductOrThrow(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.orElseThrow(
                () -> new RuntimeException("Some products not found - transaction will rollback!"));
    }

    private static long parseProductIdKey(String key) {
        try {
            return Long.parseLong(key.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Некорректный ID товара: " + key);
        }
    }

    private static double applyDeliveryFee(double itemsTotal) {
        if (itemsTotal <= 0 || itemsTotal >= FREE_DELIVERY_THRESHOLD) {
            return itemsTotal;
        }
        return itemsTotal + DELIVERY_FEE;
    }
}