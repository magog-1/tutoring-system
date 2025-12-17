package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Review;
import com.tutoring.model.Student;
import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.repository.LessonRepository;
import com.tutoring.repository.StudentRepository;
import com.tutoring.repository.SubjectRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.service.LessonService;
import com.tutoring.service.ReviewService;
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
    private ReviewService reviewService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LessonRepository lessonRepository;

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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            Long tutorId = Long.valueOf(bookingRequest.get("tutorId").toString());
            Long subjectId = Long.valueOf(bookingRequest.get("subjectId").toString());
            String scheduledTimeStr = bookingRequest.get("scheduledTime").toString();
            Integer durationMinutes = Integer.valueOf(bookingRequest.get("durationMinutes").toString());
            BigDecimal price = new BigDecimal(bookingRequest.get("price").toString());

            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

            Lesson lesson = new Lesson();
            lesson.setStudent(student);
            lesson.setTutor(tutor);
            lesson.setSubject(subject);
            lesson.setScheduledTime(LocalDateTime.parse(scheduledTimeStr));
            lesson.setDurationMinutes(durationMinutes);
            lesson.setPrice(price);

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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            List<Lesson> lessons = lessonService.getStudentLessons(student);
            
            // Явно загружаем LAZY ассоциации для сериализации
            lessons.forEach(lesson -> {
                lesson.getTutor().getFirstName(); // триггер для загрузки
                lesson.getSubject().getName();
            });
            
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> reviewRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            Long tutorId = Long.valueOf(reviewRequest.get("tutorId").toString());
            Integer rating = Integer.valueOf(reviewRequest.get("rating").toString());
            String comment = reviewRequest.get("comment").toString();

            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            Review review = new Review();
            review.setStudent(student);
            review.setTutor(tutor);
            review.setRating(rating);
            review.setComment(comment);

            // Если указан lessonId
            if (reviewRequest.containsKey("lessonId")) {
                Long lessonId = Long.valueOf(reviewRequest.get("lessonId").toString());
                Lesson lesson = lessonRepository.findById(lessonId)
                        .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));
                review.setLesson(lesson);
            }

            Review created = reviewService.createReview(review);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка при создании отзыва: " + e.getMessage());
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

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok("Отзыв удалён");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }
}
