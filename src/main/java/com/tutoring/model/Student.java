package com.tutoring.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@JsonIgnoreProperties({"lessons"})
public class Student extends User {

    @Column(length = 1000)
    private String learningGoals;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    public Student() {}

    public Student(String learningGoals, EducationLevel educationLevel) {
        this.learningGoals = learningGoals;
        this.educationLevel = educationLevel;
    }

    public String getLearningGoals() {
        return learningGoals;
    }

    public void setLearningGoals(String learningGoals) {
        this.learningGoals = learningGoals;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public enum EducationLevel {
        ELEMENTARY, MIDDLE_SCHOOL, HIGH_SCHOOL,
        UNDERGRADUATE, GRADUATE, PROFESSIONAL
    }
}
