// src/main/java/com/algoarena/model/InterviewTopic.java

package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Document(collection = "interview_topics")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewTopic {
    
    @Id
    private String id;
    
    @NotBlank(message = "Topic name is required")
    @Size(min = 2, max = 100, message = "Topic name must be between 2 and 100 characters")
    @Indexed(unique = true)
    private String name; // e.g., "React", "Java", "DBMS", "OOPs"
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private String slug; // URL-friendly version of name
    
    private String icon; // Optional icon URL from Cloudinary
    
    private String color; // Optional color for UI display (hex code)
    
    private int displayOrder; // Display order in the list
    
    private boolean isActive = true;
    
    private int documentCount = 0; // Cached count of documents
    
    private String createdBy; // Admin/SuperAdmin user ID
    
    private String updatedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public InterviewTopic() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Generate slug from name
    public void generateSlug() {
        if (this.name != null) {
            this.slug = this.name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        generateSlug();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public int getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getDocumentCount() {
        return documentCount;
    }
    
    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}