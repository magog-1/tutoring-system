package com.tutoring.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status = LessonStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 2000)
    private String notes; // Заметки репетитора

    @Column(length = 1000)
    private String homework; // Домашнее задание

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    // Конструкторы, геттеры, сеттеры

    public enum LessonStatus {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }
}
