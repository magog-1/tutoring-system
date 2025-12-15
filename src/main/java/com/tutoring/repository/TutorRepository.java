package com.tutoring.repository;

import com.tutoring.model.Subject;
import com.tutoring.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUsername(String username);

    List<Tutor> findByIsVerifiedTrue();

    List<Tutor> findBySubjectsContaining(Subject subject);

    @Query("SELECT t FROM Tutor t WHERE t.hourlyRate BETWEEN :minRate AND :maxRate")
    List<Tutor> findByHourlyRateRange(@Param("minRate") BigDecimal minRate,
                                      @Param("maxRate") BigDecimal maxRate);

    @Query("SELECT t FROM Tutor t WHERE t.rating >= :minRating ORDER BY t.rating DESC")
    List<Tutor> findByMinRating(@Param("minRating") Double minRating);
}
