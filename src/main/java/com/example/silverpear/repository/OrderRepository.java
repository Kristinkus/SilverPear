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

    @Query(value = """
               SELECT
                   o.*,
                   oi.*
               FROM orders o
               LEFT JOIN order_items oi ON o.id = oi.order_id
               WHERE o.status = :status
               """,
            nativeQuery = true)
    List<Order> findOrdersWithItemsByStatusNative(@Param("status") String status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);


    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.brand = :brand " +
            "AND o.totalAmount > :minAmount")
    List<Order> findOrdersByBrandAndStatusAndMinAmount(
            @Param("brand") String brand,
            @Param("minAmount") Double minAmount);


    @Query(value = "SELECT DISTINCT o.* FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE p.brand = :brand " +
            "AND o.total_amount > :minAmount",
            nativeQuery = true)
    List<Order> findOrdersByBrandAndStatusAndMinAmountNative(
            @Param("brand") String brand,
            @Param("minAmount") Double minAmount);
}