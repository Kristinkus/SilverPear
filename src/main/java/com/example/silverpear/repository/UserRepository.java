package com.example.silverpear.repository;

import com.example.silverpear.product.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    // Демонстрация проблемы N+1 при загрузке пользователей с заказами
    @Query("SELECT u FROM User u")
    List<User> findAllUsersWithoutOptimization();

    // Решение через @EntityGraph
    @EntityGraph(attributePaths = {"orders"})
    @Query("SELECT u FROM User u")
    List<User> findAllUsersWithOrders();
}