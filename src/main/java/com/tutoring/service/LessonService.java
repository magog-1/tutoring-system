package com.tutoring.service;

import com.tutoring.model.Lesson;
import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import com.tutoring.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    public Lesson bookLesson(Lesson lesson) {
        // Валидация времени занятия
        if (lesson.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя записаться на прошедшее время");
        }

        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setStatus(Lesson.LessonStatus.PENDING);

        return lessonRepository.save(lesson);
    }

    public void confirmLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));

        lesson.setStatus(Lesson.LessonStatus.CONFIRMED);
        lesson.setConfirmedAt(LocalDateTime.now());
        lessonRepository.save(lesson);
    }

    public void completeLesson(Long lessonId, String notes, String homework) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие не найдено"));

        lesson.setStatus(Lesson.LessonStatus.COMPLETED);
        lesson.setNotes(notes);
        lesson.setHomework(homework);
        lessonRepository.save(lesson);
    }

    public List<Lesson> getStudentLessons(Student student) {
        return lessonRepository.findByStudent(student);
    }

    public List<Lesson> getTutorLessons(Tutor tutor) {
        return lessonRepository.findByTutor(tutor);
    }
}
