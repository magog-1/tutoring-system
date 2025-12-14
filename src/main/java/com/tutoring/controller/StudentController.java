package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import com.tutoring.service.LessonService;
import com.tutoring.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private LessonService lessonService;

    @GetMapping("/tutors")
    public ResponseEntity<List<Tutor>> getAllTutors() {
        try {
            List<Tutor> tutors = tutorService.findAllVerifiedTutors();
            return ResponseEntity.ok(tutors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/lessons/book")
    public ResponseEntity<?> bookLesson(@RequestBody Lesson lesson) {
        try {
            Lesson booked = lessonService.bookLesson(lesson);
            return ResponseEntity.ok(booked);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при записи на занятие");
        }
    }

    @GetMapping("/lessons")
    public ResponseEntity<List<Lesson>> getMyLessons(@RequestParam Long studentId) {
        try {
            Student student = new Student();
            student.setId(studentId);
            List<Lesson> lessons = lessonService.getStudentLessons(student);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
