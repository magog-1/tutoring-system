package com.tutoring.controller;

import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import com.tutoring.model.User;
import com.tutoring.repository.StudentRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Необходимо указать текущий и новый пароль");
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("Пароль должен быть минимум 6 символов");
            }

            // Проверяем Student
            Optional<Student> studentOpt = studentRepository.findByUsername(username);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
                    return ResponseEntity.badRequest().body("Неверный текущий пароль");
                }
                student.setPassword(passwordEncoder.encode(newPassword));
                studentRepository.save(student);
                return ResponseEntity.ok("Пароль успешно изменен");
            }

            // Проверяем Tutor
            Optional<Tutor> tutorOpt = tutorRepository.findByUsername(username);
            if (tutorOpt.isPresent()) {
                Tutor tutor = tutorOpt.get();
                if (!passwordEncoder.matches(currentPassword, tutor.getPassword())) {
                    return ResponseEntity.badRequest().body("Неверный текущий пароль");
                }
                tutor.setPassword(passwordEncoder.encode(newPassword));
                tutorRepository.save(tutor);
                return ResponseEntity.ok("Пароль успешно изменен");
            }

            // Проверяем User (если есть другие роли)
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    return ResponseEntity.badRequest().body("Неверный текущий пароль");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok("Пароль успешно изменен");
            }

            return ResponseEntity.badRequest().body("Пользователь не найден");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка при смене пароля: " + e.getMessage());
        }
    }
}
