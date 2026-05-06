package com.example.silverpear.controller;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.AuthResponse;
import com.example.silverpear.product.productdto.LoginRequest;
import com.example.silverpear.product.productdto.RegisterRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.repository.UserRepository;
import com.example.silverpear.security.AppUserDetails;
import com.example.silverpear.security.JwtService;
import com.example.silverpear.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int MAX_LOGIN_DIGITS = 15;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        validateLoginDigitLimit(request.getLogin());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
            AppUserDetails principal = (AppUserDetails) auth.getPrincipal();
            User user = userRepository.findByLogin(principal.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            String token = jwtService.generateToken(principal);
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getId(),
                    user.getLogin(),
                    user.getName(),
                    user.getSurname(),
                    user.getPatronymic(),
                    user.getRole().name()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        UserResponse created = userService.register(request);
        User user = userRepository.findById(created.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        AppUserDetails principal = new AppUserDetails(user);
        String token = jwtService.generateToken(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getSurname(),
                user.getPatronymic(),
                user.getRole().name()));
    }

    private static void validateLoginDigitLimit(String login) {
        if (login == null) {
            return;
        }
        String trimmed = login.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        boolean phoneLike = trimmed.startsWith("+") || Character.isDigit(trimmed.charAt(0));
        if (!phoneLike) {
            return;
        }
        int digitsCount = trimmed.replaceAll("[^0-9]", "").length();
        if (digitsCount > MAX_LOGIN_DIGITS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Логин: не более 15 цифр");
        }
    }
}
