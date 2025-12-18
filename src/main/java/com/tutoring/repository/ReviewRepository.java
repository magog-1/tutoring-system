package com.tutoring.repository;

import com.tutoring.model.Review;
import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTutor(Tutor tutor);

    List<Review> findByStudent(Student student);

    // Метод для получения отзывов по ID репетитора
    @Query("SELECT r FROM Review r WHERE r.tutor.id = :tutorId")
    List<Review> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT r FROM Review r WHERE r.tutor.id = :tutorId ORDER BY r.createdAt DESC")
    List<Review> findByTutorIdOrderByDateDesc(@Param("tutorId") Long tutorId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutor.id = :tutorId")
    Double calculateAverageRatingForTutor(@Param("tutorId") Long tutorId);

    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating")
    List<Review> findByMinRating(@Param("minRating") Integer minRating);

    boolean existsByStudentAndTutor(Student student, Tutor tutor);
}
