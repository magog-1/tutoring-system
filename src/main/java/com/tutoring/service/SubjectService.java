package com.tutoring.service;

import com.tutoring.model.Subject;
import com.tutoring.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    public Subject createSubject(Subject subject) {
        if (subjectRepository.existsByName(subject.getName())) {
            throw new IllegalArgumentException("Предмет с таким названием уже существует");
        }
        return subjectRepository.save(subject);
    }

    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    public Optional<Subject> findByName(String name) {
        return subjectRepository.findByName(name);
    }

    public List<Subject> findAll() {
        return subjectRepository.findAllOrderedByName();
    }

    public List<Subject> findByCategory(Subject.SubjectCategory category) {
        return subjectRepository.findByCategory(category);
    }

    public Subject updateSubject(Subject subject) {
        if (!subjectRepository.existsById(subject.getId())) {
            throw new IllegalArgumentException("Предмет не найден");
        }
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Предмет не найден");
        }
        subjectRepository.deleteById(subjectId);
    }
}
