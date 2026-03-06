package com.example.silverpear.product.mapper;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import org.springframework.stereotype.Component;

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
        return response;
    }
}