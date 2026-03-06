package com.example.silverpear.service;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        user.getFavorites().add(product);
        userRepository.save(user); // каскад не нужен, но сохраняем связь
    }

    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        user.getFavorites().remove(product);
        userRepository.save(user);
    }

    public Set<Product> getFavorites(Long userId) {
        return favoriteRepository.findFavoritesByUserId(userId);
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.findFavoritesByUserId(userId)
                .stream().anyMatch(p -> p.getId().equals(productId));
    }
}