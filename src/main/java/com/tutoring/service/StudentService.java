package com.tutoring.service;

import com.tutoring.model.Student;
import com.tutoring.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Student createStudent(Student student) {
        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Студент с таким email уже существует");
        }
        return studentRepository.save(student);
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public List<Student> findAllActiveStudents() {
        return studentRepository.findAllActiveStudents();
    }

    public Student updateStudent(Student student) {
        if (!studentRepository.existsById(student.getId())) {
            throw new IllegalArgumentException("Студент не найден");
        }
        return studentRepository.save(student);
    }

    public void deactivateStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        student.setIsActive(false);
        studentRepository.save(student);
    }

    public List<Student> findByEducationLevel(Student.EducationLevel level) {
        return studentRepository.findByEducationLevel(level);
    }
}
