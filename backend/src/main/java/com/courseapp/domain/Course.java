package com.courseapp.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Course {
    private Long id;
    private Long instructorId;
    private String title;
    private String description;
    private BigDecimal price;
    private String thumbnail;
    private Boolean isPublished;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined / computed fields
    private String instructorName;
    private Integer studentCount;
    private Integer moduleCount;
    private BigDecimal totalRevenue;

    public Course() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}