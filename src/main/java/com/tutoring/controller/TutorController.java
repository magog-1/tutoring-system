package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Tutor;
import com.tutoring.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    @Autowired
    private LessonService lessonService;

    @GetMapping("/lessons")
    public ResponseEntity<List<Lesson>> getMyLessons(@RequestParam Long tutorId) {
        try {
            Tutor tutor = new Tutor();
            tutor.setId(tutorId);
            List<Lesson> lessons = lessonService.getTutorLessons(tutor);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/lessons/{lessonId}/confirm")
    public ResponseEntity<?> confirmLesson(@PathVariable Long lessonId) {
        try {
            lessonService.confirmLesson(lessonId);
            return ResponseEntity.ok("Занятие подтверждено");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при подтверждении");
        }
    }

    @PutMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<?> completeLesson(
            @PathVariable Long lessonId,
            @RequestParam String notes,
            @RequestParam(required = false) String homework) {
        try {
            lessonService.completeLesson(lessonId, notes, homework);
            return ResponseEntity.ok("Занятие завершено");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при завершении занятия");
        }
    }
}
