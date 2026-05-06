package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.AdminUserListDto;
import com.example.silverpear.product.productdto.AdminUserOrderSummaryDto;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPatronymic(request.getPatronymic());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setPatronymic(user.getPatronymic());
        if (user.getRole() != null) {
            response.setRole(user.getRole().name());
        }
        return response;
    }

    public AdminUserListDto toAdminListDto(User user) {
        if (user == null) {
            return null;
        }
        AdminUserListDto dto = new AdminUserListDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setPatronymic(user.getPatronymic());
        dto.setPhone(user.getPhone());
        dto.setPasswordMasked("********");
        List<AdminUserOrderSummaryDto> orderDtos = user.getOrders().stream()
                .sorted(Comparator.comparing(
                        order -> order.getOrderDate(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(order -> {
                    AdminUserOrderSummaryDto orderDto = new AdminUserOrderSummaryDto();
                    orderDto.setId(order.getId());
                    orderDto.setOrderNumber(order.getOrderNumber());
                    orderDto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
                    orderDto.setOrderDate(order.getOrderDate());
                    orderDto.setTotalAmount(order.getTotalAmount());
                    return orderDto;
                })
                .toList();
        dto.setOrders(orderDtos);
        return dto;
    }
}