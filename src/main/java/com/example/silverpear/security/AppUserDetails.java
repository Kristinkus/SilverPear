package com.example.silverpear.security;

import com.example.silverpear.enums.UserRole;
import com.example.silverpear.product.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String login;
    private final String password;
    private final UserRole role;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.password = user.getPassword();
        this.role = user.getRole() != null ? user.getRole() : UserRole.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return login;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
