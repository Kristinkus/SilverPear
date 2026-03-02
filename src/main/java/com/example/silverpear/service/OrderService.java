package com.example.silverpear.service;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.OrderItem;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.OrderRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.silverpear.product.productdto.OrderRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
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


    public Order createOrderWithoutTransaction(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));


        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
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
        return orderRepository.save(savedOrder);
    }

    @Transactional
    public Order createOrderWithTransaction(OrderRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        List<Product> products = new ArrayList<>();
        for (Long productId : request.getProductIds()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Some products not found - transaction will rollback!"));
            products.add(product);
        }

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setUser(user);

        Order savedOrder = orderRepository.save(order);

        double totalAmount = 0.0;


        for (int i = 0; i < request.getProductIds().size(); i++) {
            Product product = products.get(i);
            Integer quantity = request.getQuantities().get(i);

            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            item.setPriceAtTime(product.getSalePrice());
            item.setProduct(product);
            item.setOrder(savedOrder);

            savedOrder.addOrderItem(item);
            totalAmount += product.getSalePrice() * quantity;
        }

        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }

}