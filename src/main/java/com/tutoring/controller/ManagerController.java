package com.tutoring.controller;

import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.model.User;
import com.tutoring.repository.UserRepository;
import com.tutoring.service.SubjectService;
import com.tutoring.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/tutors/pending")
    public ResponseEntity<?> getPendingTutors() {
        try {
            // Получаем всех репетиторов, ожидающих верификации
            List<Tutor> pendingTutors = tutorService.findAllVerifiedTutors();
            return ResponseEntity.ok(pendingTutors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении списка репетиторов");
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

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody Subject subject) {
        try {
            Subject created = subjectService.createSubject(subject);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при создании предмета");
        }
    }

    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<?> updateSubject(@PathVariable Long subjectId, @RequestBody Subject subject) {
        try {
            subject.setId(subjectId);
            Subject updated = subjectService.updateSubject(subject);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при обновлении предмета");
        }
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            return ResponseEntity.ok("Предмет удален");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при удалении предмета");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String role) {
        try {
            List<User> users;
            if (role != null && !role.isEmpty()) {
                User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
                users = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == userRole)
                        .toList();
            } else {
                users = userRepository.findAll();
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении пользователей");
        }
    }
}
