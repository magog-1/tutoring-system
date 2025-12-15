package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Student;
import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.repository.StudentRepository;
import com.tutoring.repository.SubjectRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.service.LessonService;
import com.tutoring.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private SubjectRepository subjectRepository;

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
    public ResponseEntity<?> bookLesson(@RequestBody Map<String, Object> bookingRequest) {
        try {
            // Получаем текущего авторизованного пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Находим студента по username
            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            // Извлекаем данные из запроса
            Long tutorId = Long.valueOf(bookingRequest.get("tutorId").toString());
            Long subjectId = Long.valueOf(bookingRequest.get("subjectId").toString());
            String scheduledTimeStr = bookingRequest.get("scheduledTime").toString();
            Integer durationMinutes = Integer.valueOf(bookingRequest.get("durationMinutes").toString());
            BigDecimal price = new BigDecimal(bookingRequest.get("price").toString());

            // Находим репетитора и предмет
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

            // Создаём объект занятия
            Lesson lesson = new Lesson();
            lesson.setStudent(student);
            lesson.setTutor(tutor);
            lesson.setSubject(subject);
            lesson.setScheduledTime(LocalDateTime.parse(scheduledTimeStr));
            lesson.setDurationMinutes(durationMinutes);
            lesson.setPrice(price);

            // Бронируем занятие
            Lesson booked = lessonService.bookLesson(lesson);
            return ResponseEntity.ok(booked);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка при записи на занятие: " + e.getMessage());
        }
    }

    @GetMapping("/lessons")
    public ResponseEntity<?> getMyLessons() {
        try {
            // Получаем текущего авторизованного пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Находим студента
            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            List<Lesson> lessons = lessonService.getStudentLessons(student);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<?> cancelLesson(@PathVariable Long lessonId) {
        try {
            // TODO: Реализовать отмену занятия
            return ResponseEntity.ok("Занятие отменено");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }
}
