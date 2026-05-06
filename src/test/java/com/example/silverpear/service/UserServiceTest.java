package com.example.silverpear.service;

import com.example.silverpear.enums.UserRole;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.mapper.UserMapper;
import com.example.silverpear.product.mapper.UserWithOrdersMapper;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import com.example.silverpear.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserWithOrdersMapper userWithOrdersMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, userWithOrdersMapper, passwordEncoder);
        lenient().when(passwordEncoder.encode(any())).thenAnswer(invocation -> {
            Object raw = invocation.getArgument(0);
            return "enc_" + raw;
        });
    }

    @Test
    void getUserById_success() {
        User user = new User();
        UserResponse response = new UserResponse();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        assertEquals(response, userService.getUserById(1L));
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        long userId = 1L;
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.getUserById(userId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getUserWithOrders_success() {
        User user = new User();
        UserWithOrdersDto dto = new UserWithOrdersDto();
        when(userRepository.findByIdWithOrdersAndItems(2L)).thenReturn(Optional.of(user));
        when(userWithOrdersMapper.toDto(user)).thenReturn(dto);
        assertEquals(dto, userService.getUserWithOrders(2L));
    }

    @Test
    void getUserWithOrders_notFound() {
        when(userRepository.findByIdWithOrdersAndItems(3L)).thenReturn(Optional.empty());
        long userId = 3L;
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.getUserWithOrders(userId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createUser_success() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        request.setPassword("password12");
        request.setName("n");
        request.setSurname("s");
        request.setEmail("e@e.ru");
        request.setPhone("+7 999 123 45 67");
        User user = new User();
        User saved = new User();
        UserResponse response = new UserResponse();

        when(userRepository.existsByLogin("l")).thenReturn(false);
        when(userRepository.existsByEmail("e@e.ru")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, userService.createUser(request));
    }

    @Test
    void createUser_conflictLogin() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        when(userRepository.existsByLogin("l")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createUser_conflictEmail() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        request.setEmail("e");
        when(userRepository.existsByLogin("l")).thenReturn(false);
        when(userRepository.existsByEmail("e")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateUser_success() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        request.setPassword("password12");
        request.setName("n");
        request.setSurname("s");
        request.setEmail("e");
        request.setPhone("+7 999 123 45 67");
        User existing = new User();
        existing.setId(1L);
        existing.setRole(UserRole.USER);
        User entity = new User();
        User saved = new User();
        UserResponse response = new UserResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByLoginAndIdNot("l", 1L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("e", 1L)).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, userService.updateUser(1L, request));
        assertEquals(Long.valueOf(1L), entity.getId());
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserRequest missing = new UserRequest();
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(1L, missing));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateUser_loginConflict() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        request.setPassword("password12");
        request.setName("n");
        request.setSurname("s");
        request.setEmail("e@e.ru");
        request.setPhone("+7 999 123 45 67");
        User existing = new User();
        existing.setId(1L);
        existing.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByLoginAndIdNot("l", 1L)).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateUser_emailConflict() {
        UserRequest request = new UserRequest();
        request.setLogin("l");
        request.setPassword("password12");
        request.setName("n");
        request.setSurname("s");
        request.setEmail("e");
        request.setPhone("+7 999 123 45 67");
        User existing = new User();
        existing.setId(1L);
        existing.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByLoginAndIdNot("l", 1L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("e", 1L)).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getAllUsers_success() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);
        assertEquals(users, userService.getAllUsers());
    }
}
