package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserWithOrdersMapper {

    private final OrderForUserMapper orderForUserMapper;

    public UserWithOrdersDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserWithOrdersDto dto = new UserWithOrdersDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setPatronymic(user.getPatronymic());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        if (user.getGiftBalance() != null) {
            dto.setGiftBalance(user.getGiftBalance().doubleValue());
        } else {
            dto.setGiftBalance(0.0);
        }
        dto.setOrders(user.getOrders().stream()
                .map(orderForUserMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
}