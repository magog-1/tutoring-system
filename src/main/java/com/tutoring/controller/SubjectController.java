package com.tutoring.controller;

import com.tutoring.model.Subject;
import com.tutoring.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping
    public ResponseEntity<?> getAllSubjects() {
        try {
            List<Subject> subjects = subjectService.findAll();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении предметов");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id) {
        try {
            Subject subject = subjectService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));
            return ResponseEntity.ok(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении предмета");
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getSubjectsByCategory(@PathVariable String category) {
        try {
            Subject.SubjectCategory subjectCategory = Subject.SubjectCategory.valueOf(category.toUpperCase());
            List<Subject> subjects = subjectService.findByCategory(subjectCategory);
            return ResponseEntity.ok(subjects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Неверная категория");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении предметов");
        }
    }
}
