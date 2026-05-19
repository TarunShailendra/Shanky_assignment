package com.courseapp.domain;

import java.time.LocalDateTime;

public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDateTime enrolledAt;
    private String status;

    // Joined fields
    private String title;
    private String description;
    private String thumbnail;
    private java.math.BigDecimal price;
    private String category;
    private String instructorName;
    private Integer moduleCount;
    private Integer assignmentCount;
    private String fullName;

    public Enrollment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }
    public Integer getAssignmentCount() { return assignmentCount; }
    public void setAssignmentCount(Integer assignmentCount) { this.assignmentCount = assignmentCount; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}