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

    List<Order> findByUser(User user);

    @Query("SELECT o FROM Order o")
    List<Order> findAllOrdersWithoutOptimization();

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o")
    List<Order> findAllOrdersWithItemsAndProducts();
}