package com.example.silverpear.repository;

import com.example.silverpear.enums.OrderStatus;
import com.example.silverpear.product.entity.Order;
import com.example.silverpear.product.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    @Query("SELECT o FROM Order o LEFT JOIN FETCH OrderItem oi ON o.id = oi.id WHERE o.status = :status ")
    List<Order> findOrderByStatus(@Param("status") OrderStatus status);

    @Query(value = "SELECT\n" +
                   "    o.*,\n" +
                   "    oi.*\n" +
                   "FROM orders o\n" +
                   "LEFT JOIN order_items oi ON o.id = oi.order_id\n" +
                   "WHERE o.status = :status\n",
            nativeQuery = true)
    List<Order> findOrdersWithItemsByStatusNative(@Param("status") String status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);



}