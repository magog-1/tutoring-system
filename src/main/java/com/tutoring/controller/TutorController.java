package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Review;
import com.tutoring.model.Tutor;
import com.tutoring.repository.TutorRepository;
import com.tutoring.service.LessonService;
import com.tutoring.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Tutor tutor = tutorRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            // Явно загружаем subjects для сериализации
            if (tutor.getSubjects() != null) {
                tutor.getSubjects().size();
            }

            return ResponseEntity.ok(tutor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Tutor tutor = tutorRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            // Обновляем основные поля
            if (updates.containsKey("firstName")) {
                tutor.setFirstName(updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                tutor.setLastName(updates.get("lastName"));
            }
            if (updates.containsKey("phoneNumber")) {
                tutor.setPhoneNumber(updates.get("phoneNumber"));
            }
            if (updates.containsKey("education")) {
                tutor.setEducation(updates.get("education"));
            }
            if (updates.containsKey("experienceYears")) {
                tutor.setExperienceYears(Integer.parseInt(updates.get("experienceYears")));
            }
            if (updates.containsKey("hourlyRate")) {
                tutor.setHourlyRate(new BigDecimal(updates.get("hourlyRate")));
            }
            if (updates.containsKey("bio")) {
                tutor.setBio(updates.get("bio"));
            }

            Tutor updated = tutorRepository.save(tutor);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка обновления профиля: " + e.getMessage());
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getMyReviews() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Tutor tutor = tutorRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            List<Review> reviews = reviewService.getReviewsByTutor(tutor.getId());
            
            // Загружаем связанные данные
            reviews.forEach(review -> {
                if (review.getStudent() != null) {
                    review.getStudent().getFirstName();
                }
            });

            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка загрузки отзывов: " + e.getMessage());
        }
    }

    @GetMapping("/lessons")
    public ResponseEntity<?> getMyLessons() {
        try {
            // Получаем текущего авторизованного пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Находим репетитора
            Tutor tutor = tutorRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            List<Lesson> lessons = lessonService.getTutorLessons(tutor);
            
            // Явно загружаем LAZY ассоциации для сериализации
            lessons.forEach(lesson -> {
                lesson.getStudent().getFirstName(); // триггер для загрузки Student
                lesson.getSubject().getName();      // триггер для загрузки Subject
            });
            
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
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

    @PutMapping("/lessons/{lessonId}/cancel")
    public ResponseEntity<?> cancelLesson(@PathVariable Long lessonId) {
        try {
            lessonService.cancelLesson(lessonId);
            return ResponseEntity.ok("Занятие отклонено");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при отклонении");
        }
    }

    @PutMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<?> completeLesson(
            @PathVariable Long lessonId,
            @RequestBody Map<String, String> request) {
        try {
            String notes = request.get("notes");
            String homework = request.get("homework");
            lessonService.completeLesson(lessonId, notes, homework);
            return ResponseEntity.ok("Занятие завершено");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при завершении занятия");
        }
    }
}
