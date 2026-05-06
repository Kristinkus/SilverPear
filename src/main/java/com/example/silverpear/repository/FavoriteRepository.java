package com.example.silverpear.repository;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.favorites FROM User u WHERE u.id = :userId")
    java.util.Set<Product> findFavoritesByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM user_favorites", nativeQuery = true)
    void clearAllFavoritesLinks();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM user_favorite_brands", nativeQuery = true)
    void clearAllFavoriteBrandLinks();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM user_favorites WHERE product_id = :productId", nativeQuery = true)
    void deleteFavoriteLinksByProductId(@Param("productId") Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM user_favorites WHERE user_id = :userId AND product_id = :productId", nativeQuery = true)
    int deleteFavoriteLink(@Param("userId") Long userId, @Param("productId") Long productId);
}