package com.tutoring.service;

import com.tutoring.model.Review;
import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import com.tutoring.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TutorService tutorService;

    public Review createReview(Review review) {
        // Валидация рейтинга
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Рейтинг должен быть от 1 до 5");
        }

        // Проверка, что студент еще не оставлял отзыв этому репетитору
        if (reviewRepository.existsByStudentAndTutor(review.getStudent(), review.getTutor())) {
            throw new IllegalArgumentException("Вы уже оставили отзыв этому репетитору");
        }

        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        // Обновляем рейтинг репетитора
        tutorService.updateRating(review.getTutor().getId());

        return savedReview;
    }

    public List<Review> getReviewsForTutor(Long tutorId) {
        Tutor tutor = new Tutor();
        tutor.setId(tutorId);
        return reviewRepository.findByTutor(tutor);
    }

    public List<Review> getReviewsByStudent(Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        return reviewRepository.findByStudent(student);
    }

    public Double getAverageRatingForTutor(Long tutorId) {
        return reviewRepository.calculateAverageRatingForTutor(tutorId);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        Long tutorId = review.getTutor().getId();
        reviewRepository.deleteById(reviewId);

        // Обновляем рейтинг репетитора после удаления отзыва
        tutorService.updateRating(tutorId);
    }
}
