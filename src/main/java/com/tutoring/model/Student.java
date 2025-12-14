package com.tutoring.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student extends User {

    @Column(length = 1000)
    private String learningGoals;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    // Конструкторы, геттеры, сеттеры

    public enum EducationLevel {
        ELEMENTARY, MIDDLE_SCHOOL, HIGH_SCHOOL,
        UNDERGRADUATE, GRADUATE, PROFESSIONAL
    }
}
