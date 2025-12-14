package com.tutoring.repository;

import com.tutoring.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByName(String name);

    List<Subject> findByCategory(Subject.SubjectCategory category);

    @Query("SELECT s FROM Subject s ORDER BY s.name ASC")
    List<Subject> findAllOrderedByName();

    boolean existsByName(String name);
}
