package com.tutoring.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutors")
public class Tutor extends User {

    @Column(length = 2000)
    private String bio;

    @Column(nullable = false)
    private String education;

    @Column(nullable = false)
    private Integer experienceYears;

    @Column(nullable = false)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private Boolean isVerified = false;

    private Double rating = 0.0;

    private Integer totalReviews = 0;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "tutor_subjects",
            joinColumns = @JoinColumn(name = "tutor_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    // Конструкторы, геттеры, сеттеры
}
