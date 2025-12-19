package com.tutoring.service;

import com.tutoring.model.Review;
import com.tutoring.model.Tutor;
import com.tutoring.repository.ReviewRepository;
import com.tutoring.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Transactional
    public Review createReview(Review review) {
        Review saved = reviewRepository.save(review);

        // Обновляем рейтинг репетитора
        updateTutorRating(review.getTutor().getId());

        return saved;
    }

    public List<Review> getReviewsByTutor(Long tutorId) {
        return reviewRepository.findByTutorId(tutorId);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        
        Long tutorId = review.getTutor().getId();
        reviewRepository.deleteById(reviewId);
        
        // Пересчитываем рейтинг
        updateTutorRating(tutorId);
    }

    private void updateTutorRating(Long tutorId) {
        List<Review> reviews = reviewRepository.findByTutorId(tutorId);
        if (reviews.isEmpty()) {
            return;
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
        
        tutor.setRating(averageRating);
        tutor.setTotalReviews(reviews.size());
        tutorRepository.save(tutor);
    }
}
