package com.tutoring.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private SubjectCategory category;

    @ManyToMany(mappedBy = "subjects")
    private List<Tutor> tutors = new ArrayList<>();

    // Конструкторы, геттеры, сеттеры

    public enum SubjectCategory {
        MATHEMATICS, LANGUAGES, SCIENCES,
        ARTS, PROGRAMMING, OTHER
    }
}
