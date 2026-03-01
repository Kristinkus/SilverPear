package com.example.silverpear.repository;

import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Проблема N+1: каждый заказ будет делать отдельный запрос для загрузки orderItems и product
    List<Order> findByUser(User user);

    // Решение через @EntityGraph (загружает все связанные сущности одним запросом)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o WHERE o.user = :user")
    List<Order> findByUserWithItemsAndProducts(User user);

    // Решение через fetch join (альтернативный способ)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user = :user")
    List<Order> findByUserWithItemsAndProductsFetchJoin(User user);

    // Демонстрация проблемы N+1
    @Query("SELECT o FROM Order o")
    List<Order> findAllOrdersWithoutOptimization();

    // Оптимизированный запрос с @EntityGraph
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o")
    List<Order> findAllOrdersWithOptimization();
}