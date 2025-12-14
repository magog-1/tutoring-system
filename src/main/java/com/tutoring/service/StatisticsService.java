package com.tutoring.service;

import com.tutoring.model.Lesson;
import com.tutoring.repository.LessonRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private LessonRepository lessonRepository;

    public Map<String, Object> getGeneralStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalTutors", tutorRepository.count());
        stats.put("completedLessons", lessonRepository.countCompletedLessons());
        stats.put("averageWaitingTime", calculateAverageWaitingTime());

        return stats;
    }

    private Double calculateAverageWaitingTime() {
        List<Lesson> confirmedLessons = lessonRepository.findByStatus(Lesson.LessonStatus.CONFIRMED);

        if (confirmedLessons.isEmpty()) {
            return 0.0;
        }

        double totalHours = confirmedLessons.stream()
                .filter(l -> l.getConfirmedAt() != null && l.getCreatedAt() != null)
                .mapToLong(l -> Duration.between(l.getCreatedAt(), l.getConfirmedAt()).toHours())
                .average()
                .orElse(0.0);

        return totalHours;
    }

    public Map<String, Long> getSubjectPopularity() {
        List<Lesson> allLessons = lessonRepository.findAll();

        Map<String, Long> subjectCount = new HashMap<>();
        allLessons.forEach(lesson -> {
            String subjectName = lesson.getSubject().getName();
            subjectCount.merge(subjectName, 1L, Long::sum);
        });

        return subjectCount;
    }
}
