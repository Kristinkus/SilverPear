package com.example.silverpear.api;

import com.example.silverpear.errors.ErrorResponse;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.OrderForUserDto;
import com.example.silverpear.product.productdto.OrderRequest;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Регистрация, профиль, заказы пользователя")
public interface UserApi {

    @GetMapping
    @Operation(summary = "Список всех пользователей")
    ResponseEntity<List<User>> getAllUsers();

    @GetMapping("/{id}")
    @Operation(summary = "Пользователь по id")
    ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Id пользователя") @PathVariable Long id);

    @GetMapping("/{id}/orders")
    @Operation(summary = "Пользователь с заказами")
    ResponseEntity<UserWithOrdersDto> getUserWithOrders(
            @Parameter(description = "Id пользователя") @PathVariable Long id);

    @PostMapping
    @Operation(summary = "Создать пользователя")
    @ApiResponses({ @ApiResponse(responseCode = "201", description = "Создано"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Логин или email уже заняты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request);

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Обновлено"),
                   @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                   content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                   @ApiResponse(responseCode = "409", description = "Конфликт данных",
                   content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "Id пользователя") @PathVariable Long id,
            @Valid @RequestBody UserRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "204", description = "Удалено")
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "Id пользователя") @PathVariable Long id);

    @PostMapping("/{userId}/orders")
    @Operation(summary = "Создать заказ для пользователя")
    ResponseEntity<OrderForUserDto> createOrderForUser(
            @Parameter(description = "Id пользователя") @PathVariable Long userId,
            @RequestBody OrderRequest request);
}
