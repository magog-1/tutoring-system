package com.tutoring.repository;

import com.tutoring.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByUsername(String username);

    List<Student> findByEducationLevel(Student.EducationLevel level);

    @Query("SELECT s FROM Student s WHERE s.isActive = true")
    List<Student> findAllActiveStudents();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.isActive = true")
    Long countActiveStudents();
}
