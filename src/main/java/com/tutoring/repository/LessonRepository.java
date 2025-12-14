package com.tutoring.repository;

import com.tutoring.model.Lesson;
import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByStudent(Student student);

    List<Lesson> findByTutor(Tutor tutor);

    List<Lesson> findByStatus(Lesson.LessonStatus status);

    @Query("SELECT l FROM Lesson l WHERE l.scheduledTime >= :startDate AND l.scheduledTime <= :endDate")
    List<Lesson> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.status = 'COMPLETED'")
    Long countCompletedLessons();
}
