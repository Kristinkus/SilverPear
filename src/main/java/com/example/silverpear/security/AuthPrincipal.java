package com.example.silverpear.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthPrincipal {

    public AppUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется вход");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof AppUserDetails user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется вход");
        }
        return user;
    }

    public void requireAdmin() {
        if (!currentUser().isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нужны права администратора");
        }
    }

    public void requireSelfOrAdmin(Long userId) {
        AppUserDetails u = currentUser();
        if (!u.isAdmin() && !u.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к данным этого профиля");
        }
    }
}
