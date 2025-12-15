package com.tutoring.client.model;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private UserDTO student;
    private TutorDTO tutor;
    private LessonDTO lesson;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserDTO getStudent() { return student; }
    public void setStudent(UserDTO student) { this.student = student; }
    
    public TutorDTO getTutor() { return tutor; }
    public void setTutor(TutorDTO tutor) { this.tutor = tutor; }
    
    public LessonDTO getLesson() { return lesson; }
    public void setLesson(LessonDTO lesson) { this.lesson = lesson; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
