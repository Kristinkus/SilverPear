package com.example.silverpear.service;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrderWithItems(Long userId, List<Long> productIds, List<Integer> quantities) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setUser(user);

        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            final int index = i;

            Product product = productRepository.findById(productIds.get(index))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productIds.get(index)));

            OrderItem item = new OrderItem();
            item.setQuantity(quantities.get(index));
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);

            savedOrder.addOrderItem(item);

            totalAmount += product.getSalePrice() * quantities.get(index);

            if (index == 1 && productIds.size() > 2) {
                throw new DataIntegrityViolationException("Имитация ошибки при " +
                        "создании заказа - транзакция будет откачена");
            }
        }

        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }

    public void demonstrateNPlusOneProblem(Long userId) {
        //тут была визуализация проблем, но сейчас уже нет:)
    }


    public void demonstrateSolutionWithEntityGraph(Long userId) {
        //тут была визуализация проблемы, но сейчас уже нет:)
    }


    public Order createOrderWithoutTransaction(Long userId, List<Long> productIds, List<Integer> quantities) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setUser(user);


        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            final int index = i;
            Long productId = productIds.get(index);
            Integer quantity = quantities.get(index);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);


            if (index == 2) {
                throw new DataIntegrityViolationException("Ошибка при добавлении товара - " +
                        "заказ уже сохранен без товаров");
            }

            savedOrder.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }

        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }
}