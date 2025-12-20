package com.tutoring.controller;

import com.tutoring.model.Lesson;
import com.tutoring.model.Review;
import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.repository.SubjectRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.service.LessonService;
import com.tutoring.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SubjectRepository subjectRepository;

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
    @Transactional
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
                try {
                    tutor.setExperienceYears(Integer.parseInt(updates.get("experienceYears")));
                } catch (NumberFormatException e) {
                    // Игнорируем неверный формат
                }
            }
            if (updates.containsKey("hourlyRate")) {
                try {
                    tutor.setHourlyRate(new BigDecimal(updates.get("hourlyRate")));
                } catch (NumberFormatException e) {
                    // Игнорируем неверный формат
                }
            }
            if (updates.containsKey("bio")) {
                tutor.setBio(updates.get("bio"));
            }

            // ОБРАБОТКА ПРЕДМЕТОВ
            if (updates.containsKey("subjects")) {
                String subjectsStr = updates.get("subjects");
                if (subjectsStr != null && !subjectsStr.trim().isEmpty()) {
                    // Разбиваем по строкам
                    String[] subjectNames = subjectsStr.split("\\n");
                    Set<Subject> newSubjects = new HashSet<>();
                    
                    // Создаём финальную переменную для использования в лямбде
                    final SubjectRepository subjectRepo = this.subjectRepository;

                    for (String name : subjectNames) {
                        name = name.trim();
                        if (!name.isEmpty()) {
                            // Ищем или создаём предмет
                            final String subjectName = name; // Делаем effectively final
                            Subject subject = subjectRepo.findByName(subjectName)
                                    .orElseGet(() -> {
                                        Subject newSubject = new Subject();
                                        newSubject.setName(subjectName);
                                        return subjectRepo.save(newSubject);
                                    });
                            newSubjects.add(subject);
                        }
                    }

                    // Обновляем список предметов
                    tutor.getSubjects().clear();
                    tutor.getSubjects().addAll(newSubjects);
                } else {
                    // Если пусто - очищаем
                    tutor.getSubjects().clear();
                }
            }

            Tutor updated = tutorRepository.save(tutor);
            
            // Загружаем subjects для ответа
            if (updated.getSubjects() != null) {
                updated.getSubjects().size();
            }
            
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Tutor tutor = tutorRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

            List<Lesson> lessons = lessonService.getTutorLessons(tutor);
            
            // Явно загружаем LAZY ассоциации для сериализации
            lessons.forEach(lesson -> {
                lesson.getStudent().getFirstName();
                lesson.getSubject().getName();
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
