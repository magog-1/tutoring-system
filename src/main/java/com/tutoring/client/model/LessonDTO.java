package com.tutoring.client.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LessonDTO {
    private Long id;
    private UserDTO student;
    private TutorDTO tutor;
    private SubjectDTO subject;
    private LocalDateTime scheduledTime;
    private Integer durationMinutes;
    private String status;
    private BigDecimal price;
    private String notes;
    private String homework;
    private LocalDateTime createdAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserDTO getStudent() { return student; }
    public void setStudent(UserDTO student) { this.student = student; }
    
    public TutorDTO getTutor() { return tutor; }
    public void setTutor(TutorDTO tutor) { this.tutor = tutor; }
    
    public SubjectDTO getSubject() { return subject; }
    public void setSubject(SubjectDTO subject) { this.subject = subject; }
    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getHomework() { return homework; }
    public void setHomework(String homework) { this.homework = homework; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
