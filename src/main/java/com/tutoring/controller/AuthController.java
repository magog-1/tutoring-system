package com.tutoring.controller;

import com.tutoring.model.User;
import com.tutoring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registered = userService.registerUser(user);
            return ResponseEntity.ok(registered);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при регистрации");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Здесь должна быть логика аутентификации
            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверные учетные данные");
        }
    }

    /**
     * Получить данные текущего авторизованного пользователя
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Получаем аутентифицированного пользователя из Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Пользователь не авторизован");
            }
            
            String usernameOrEmail = authentication.getName();
            System.out.println("[DEBUG] Current user from authentication: " + usernameOrEmail);
            
            // Пытаемся найти по username
            Optional<User> userOpt = userService.findByUsername(usernameOrEmail);
            
            // Если не нашли, пробуем по email
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(usernameOrEmail);
            }
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователь не найден");
            }
            
            User user = userOpt.get();
            
            // Создаём простой DTO как Map
            Map<String, Object> userDTO = new HashMap<>();
            userDTO.put("id", user.getId());
            userDTO.put("username", user.getUsername());
            userDTO.put("email", user.getEmail());
            userDTO.put("firstName", user.getFirstName());
            userDTO.put("lastName", user.getLastName());
            userDTO.put("phoneNumber", user.getPhone()); // Клиент ожидает phoneNumber
            userDTO.put("role", user.getRole().name()); // STUDENT, TUTOR, MANAGER, ADMIN
            
            System.out.println("[DEBUG] Returning user: " + user.getUsername() + ", role: " + user.getRole().name());
            
            return ResponseEntity.ok(userDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении данных пользователя");
        }
    }
}
