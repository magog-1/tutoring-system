package com.tutoring.service;

import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import com.tutoring.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TutorService {

    @Autowired
    private TutorRepository tutorRepository;

    public Optional<Tutor> findById(Long id) {
        return tutorRepository.findById(id);
    }

    public List<Tutor> findAllVerifiedTutors() {
        return tutorRepository.findByIsVerifiedTrue();
    }

    public List<Tutor> findAllPendingTutors() {
        return tutorRepository.findByIsVerifiedFalse();
    }

    public List<Tutor> searchTutors(Subject subject, BigDecimal maxRate, Double minRating) {
        List<Tutor> tutors;
        
        if (subject != null) {
            tutors = tutorRepository.findBySubjectsContaining(subject);
        } else {
            tutors = tutorRepository.findByIsVerifiedTrue();
        }

        if (maxRate != null) {
            tutors = tutors.stream()
                    .filter(t -> t.getHourlyRate().compareTo(maxRate) <= 0)
                    .toList();
        }

        if (minRating != null) {
            tutors = tutors.stream()
                    .filter(t -> t.getRating() >= minRating)
                    .toList();
        }

        return tutors;
    }

    public void verifyTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));
        tutor.setIsVerified(true);
        tutorRepository.save(tutor);
    }

    public void updateRating(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Репетитор не найден"));

        Double avgRating = tutor.getReviews().stream()
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0.0);

        tutor.setRating(avgRating);
        tutor.setTotalReviews(tutor.getReviews().size());
        tutorRepository.save(tutor);
    }
}
