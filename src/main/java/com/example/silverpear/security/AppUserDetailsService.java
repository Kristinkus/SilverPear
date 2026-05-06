package com.example.silverpear.security;

import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.UserRepository;
import com.example.silverpear.util.PhoneLoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String lookup = resolveLoginKey(username);
        User user = userRepository.findByLogin(lookup)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new AppUserDetails(user);
    }

    private static String resolveLoginKey(String username) {
        if (username == null || username.isBlank()) {
            return "";
        }
        String t = username.trim();
        if (t.startsWith("+") || Character.isDigit(t.charAt(0))) {
            return PhoneLoginNormalizer.toLogin(t);
        }
        return t;
    }
}
