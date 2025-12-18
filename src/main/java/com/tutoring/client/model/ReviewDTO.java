package com.tutoring.client.model;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private UserDTO student;
    private TutorDTO tutor;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LessonDTO lesson;

    public ReviewDTO() {
    }

    public ReviewDTO(Long id, UserDTO student, TutorDTO tutor, Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.student = student;
        this.tutor = tutor;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getStudent() {
        return student;
    }

    public void setStudent(UserDTO student) {
        this.student = student;
    }

    public TutorDTO getTutor() {
        return tutor;
    }

    public void setTutor(TutorDTO tutor) {
        this.tutor = tutor;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LessonDTO getLesson() {
        return lesson;
    }

    public void setLesson(LessonDTO lesson) {
        this.lesson = lesson;
    }

    public String getStudentName() {
        if (student == null) return "N/A";
        String firstName = student.getFirstName() != null ? student.getFirstName() : "";
        String lastName = student.getLastName() != null ? student.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    public String getRatingStars() {
        if (rating == null) return "☆☆☆☆☆";
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return stars.toString();
    }
}
