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

    // Конструкторы
    public Subject() {}

    public Subject(String name, String description, SubjectCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SubjectCategory getCategory() {
        return category;
    }

    public void setCategory(SubjectCategory category) {
        this.category = category;
    }

    public List<Tutor> getTutors() {
        return tutors;
    }

    public void setTutors(List<Tutor> tutors) {
        this.tutors = tutors;
    }

    // Enum
    public enum SubjectCategory {
        MATHEMATICS, LANGUAGES, SCIENCES,
        ARTS, PROGRAMMING, OTHER
    }
}
