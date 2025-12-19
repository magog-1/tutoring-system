package com.tutoring.controller;

import com.tutoring.model.Review;
import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.repository.SubjectRepository;
import com.tutoring.service.ReviewService;
import com.tutoring.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class PublicTutorController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping
    public ResponseEntity<?> getAllVerifiedTutors() {
        try {
            List<Tutor> tutors = tutorService.findAllVerifiedTutors();
            // Явно загружаем subjects для каждого репетитора
            tutors.forEach(tutor -> tutor.getSubjects().size());
            return ResponseEntity.ok(tutors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении репетиторов");
        }
    }

    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getTutorById(@PathVariable Long tutorId) {
        try {
            Tutor tutor = tutorService.findById(tutorId)
                    .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
            // Явно загружаем subjects
            tutor.getSubjects().size();
            return ResponseEntity.ok(tutor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/{tutorId}/reviews")
    public ResponseEntity<?> getTutorReviews(@PathVariable Long tutorId) {
        try {
            List<Review> reviews = reviewService.getReviewsByTutor(tutorId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении отзывов");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTutors(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) Double minRating) {
        try {
            Subject subject = null;
            if (subjectId != null) {
                subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));
            }

            List<Tutor> tutors = tutorService.searchTutors(subject, maxRate, minRating);
            // Явно загружаем subjects
            tutors.forEach(tutor -> tutor.getSubjects().size());
            return ResponseEntity.ok(tutors);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при поиске");
        }
    }
}
