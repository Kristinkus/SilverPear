package com.example.silverpear.service;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Демонстрация сохранения нескольких связанных сущностей
    @Transactional  // БЕЗ ЭТОЙ АННОТАЦИИ ПРОИЗОЙДЕТ ЧАСТИЧНОЕ СОХРАНЕНИЕ
    public Order createOrderWithItems(Long userId, List<Long> productIds, List<Integer> quantities) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setUser(user);

        // Сохраняем заказ (без @Transactional здесь может быть частичное сохранение)
        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            final int index = i; // Создаем effectively final переменную

            Product product = productRepository.findById(productIds.get(index))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productIds.get(index)));

            OrderItem item = new OrderItem();
            item.setQuantity(quantities.get(index));
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);

            // Сохраняем элемент заказа
            savedOrder.addOrderItem(item);

            totalAmount += product.getSalePrice() * quantities.get(index);

            // Демонстрация отката транзакции при ошибке
            if (index == 1 && productIds.size() > 2) {
                throw new RuntimeException("Имитация ошибки при создании заказа - транзакция будет откачена");
            }
        }

        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }

    // Демонстрация проблемы N+1
    public void demonstrateNPlusOneProblem(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        System.out.println("=== ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1 ===");
        // Это создаст N+1 запрос (1 запрос на пользователя + N запросов на заказы)
        List<Order> orders = orderRepository.findByUser(user);
        for (Order order : orders) {
            // Каждый доступ к orderItems вызывает дополнительный запрос
            System.out.println("Заказ: " + order.getOrderNumber() +
                    ", количество товаров: " + order.getOrderItems().size());
        }
    }

    // Решение проблемы N+1 через @EntityGraph
    public void demonstrateSolutionWithEntityGraph(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        System.out.println("=== РЕШЕНИЕ ПРОБЛЕМЫ N+1 ЧЕРЕЗ @EntityGraph ===");
        // Один запрос загружает все связанные данные
        List<Order> orders = orderRepository.findByUserWithItemsAndProducts(user);
        for (Order order : orders) {
            // Нет дополнительных запросов, данные уже загружены
            System.out.println("Заказ: " + order.getOrderNumber() +
                    ", количество товаров: " + order.getOrderItems().size());
            for (OrderItem item : order.getOrderItems()) {
                System.out.println("  Товар: " + item.getProduct().getName() +
                        ", количество: " + item.getQuantity());
            }
        }
    }

    // Демонстрация частичного сохранения без @Transactional
    public Order createOrderWithoutTransaction(Long userId, List<Long> productIds, List<Integer> quantities) {
        // Этот метод будет вызван из контроллера без @Transactional на уровне сервиса
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setUser(user);

        // Заказ сохранится даже если дальше произойдет ошибка (частичное сохранение)
        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            final int index = i; // Создаем effectively final переменную для использования в лямбде
            Long productId = productIds.get(index);
            Integer quantity = quantities.get(index);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);

            // Ошибка на третьем товаре - но заказ уже сохранен!
            if (index == 2) {
                throw new RuntimeException("Ошибка при добавлении товара - заказ уже сохранен без товаров");
            }

            savedOrder.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }

        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }
}