package com.example.silverpear.repository;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.favorites FROM User u WHERE u.id = :userId")
    java.util.Set<Product> findFavoritesByUserId(@Param("userId") Long userId);
}