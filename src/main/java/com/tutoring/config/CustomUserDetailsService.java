package com.tutoring.config;

import com.tutoring.model.User;
import com.tutoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем пользователя по username или email
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // Проверяем, что пользователь активен
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Пользователь заблокирован");
        }

        // Создаём роль с префиксом ROLE_
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Возвращаем UserDetails с данными пользователя
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername() != null ? user.getUsername() : user.getEmail())
                .password(user.getPassword()) // Уже зашифрованный BCrypt
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}
