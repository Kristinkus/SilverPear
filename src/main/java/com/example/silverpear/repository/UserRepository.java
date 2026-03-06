package com.example.silverpear.repository;

import com.example.silverpear.product.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    @Query("SELECT u FROM User u")
    List<User> findAllUsersWithoutOptimization();

    @EntityGraph(attributePaths = {"orders"})
    @Query("SELECT u FROM User u")
    List<User> findAllUsersWithOrders();

    @EntityGraph(attributePaths = {"orders"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);

    @EntityGraph(attributePaths = {"favorites"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithFavorites(@Param("id") Long id);

    @EntityGraph(attributePaths = {"orders", "orders.orderItems", "orders.orderItems.product"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithOrdersAndItems(@Param("id") Long id);

}