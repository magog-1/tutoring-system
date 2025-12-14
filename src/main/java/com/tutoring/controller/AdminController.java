package com.tutoring.controller;

import com.tutoring.model.User;
import com.tutoring.service.TutorService;
import com.tutoring.service.UserService;
import com.tutoring.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StatisticsService statisticsService;

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long userId,
            @RequestParam User.UserRole newRole) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            user.setRole(newRole);
            userService.updateUser(user);
            return ResponseEntity.ok("Роль пользователя изменена");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при изменении роли");
        }
    }

    @PutMapping("/tutors/{tutorId}/verify")
    public ResponseEntity<?> verifyTutor(@PathVariable Long tutorId) {
        try {
            tutorService.verifyTutor(tutorId);
            return ResponseEntity.ok("Репетитор верифицирован");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при верификации");
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("Пользователь деактивирован");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при удалении пользователя");
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getGeneralStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
