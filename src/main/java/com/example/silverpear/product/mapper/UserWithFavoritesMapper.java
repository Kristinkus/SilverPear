package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.UserWithFavoritesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserWithFavoritesMapper {

    private final ProductMapper productMapper;

    public UserWithFavoritesDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserWithFavoritesDto dto = new UserWithFavoritesDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setFavorites(user.getFavorites().stream()
                .map(productMapper::toSimpleDto)
                .collect(Collectors.toSet()));
        return dto;
    }
}