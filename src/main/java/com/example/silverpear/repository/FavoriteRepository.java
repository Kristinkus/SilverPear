package com.example.silverpear.repository;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FavoriteRepository extends JpaRepository<User, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_favorites (user_id, product_id) VALUES (:userId, :productId)", nativeQuery = true)
    void addFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Transactional
    @Query(value = "DELETE FROM user_favorites WHERE user_id = :userId AND product_id = :productId", nativeQuery = true)
    void removeFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("SELECT u.favorites FROM User u WHERE u.id = :userId")
    java.util.Set<Product> findFavoritesByUserId(@Param("userId") Long userId);
}