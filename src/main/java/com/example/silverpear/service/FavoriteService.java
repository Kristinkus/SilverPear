package com.example.silverpear.service;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден. Войдите в аккаунт заново"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Товар не найден в текущем каталоге. Обновите страницу"));

        user.getFavorites().add(product);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден. Войдите в аккаунт заново");
        }
        // Надежно удаляем связь напрямую в join-таблице независимо от состояния entity в persistence context.
        favoriteRepository.deleteFavoriteLink(userId, productId);
    }

    @Transactional
    public Set<Product> getFavorites(Long userId) {
        User user = userRepository.findByIdWithFavorites(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден. Войдите в аккаунт заново"));
        Set<Product> favorites = user.getFavorites();

        // Удаляем дубли одной и той же позиции (например, импортированные клоны с разными id).
        Map<String, Product> uniqueByFingerprint = new LinkedHashMap<>();
        for (Product p : favorites) {
            if (p == null || p.getId() == null) {
                continue;
            }
            String fingerprint = buildFavoriteFingerprint(p);
            Product prev = uniqueByFingerprint.get(fingerprint);
            if (prev == null || p.getId() < prev.getId()) {
                uniqueByFingerprint.put(fingerprint, p);
            }
        }
        if (uniqueByFingerprint.size() != favorites.size()) {
            favorites.clear();
            favorites.addAll(uniqueByFingerprint.values());
            userRepository.save(user);
        }
        return favorites;
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.findFavoritesByUserId(userId)
                .stream().anyMatch(p -> p.getId().equals(productId));
    }

    private static String buildFavoriteFingerprint(Product p) {
        String name = normalizeFingerprintText(p.getName());
        String brand = normalizeFingerprintText(p.getBrand());
        String category = normalizeFingerprintText(p.getCategory());
        String image = normalizeFingerprintText(p.getImageUrl());
        String price = String.format(Locale.ROOT, "%.2f", p.getSalePrice());
        return name + "|" + brand + "|" + category + "|" + price + "|" + image;
    }

    private static String normalizeFingerprintText(String value) {
        return Objects.toString(value, "")
                .trim()
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    public Set<String> getFavoriteBrands(Long userId) {
        User user = userRepository.findByIdWithFavoriteBrands(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Set<String> favorites = user.getFavoriteBrands();
        boolean changed = favorites.removeIf(brand -> !productRepository.existsByBrandIgnoreCase(brand));
        if (changed) {
            userRepository.save(user);
        }
        return favorites;
    }

    @Transactional
    public void addFavoriteBrand(Long userId, String brand) {
        String normalizedBrand = brand == null ? "" : brand.trim();
        if (normalizedBrand.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required");
        }
        if (!productRepository.existsByBrandIgnoreCase(normalizedBrand)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Бренд не найден в текущем каталоге. Обновите страницу");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден. Войдите в аккаунт заново"));
        user.getFavoriteBrands().add(normalizedBrand);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavoriteBrand(Long userId, String brand) {
        String normalizedBrand = brand == null ? "" : brand.trim();
        if (normalizedBrand.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден. Войдите в аккаунт заново"));
        user.getFavoriteBrands().removeIf(item -> item.equalsIgnoreCase(normalizedBrand));
        userRepository.save(user);
    }

}