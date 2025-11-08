// src/main/java/com/algoarena/model/InterviewDocument.java

package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Document(collection = "interview_documents")
@CompoundIndexes({
    @CompoundIndex(name = "topic_order_idx", def = "{'topicId': 1, 'displayOrder': 1}"),
    @CompoundIndex(name = "topic_active_idx", def = "{'topicId': 1, 'isActive': 1}")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewDocument {
    
    @Id
    private String id;
    
    @NotNull(message = "Topic ID is required")
    private String topicId; // Reference to InterviewTopic
    
    @NotBlank(message = "Document title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title; // e.g., "React Concepts Part 1", "Hooks Deep Dive"
    
    private String slug; // URL-friendly version
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description; // Brief description for listing
    
    // Main content - stores whatever frontend sends as JSON string
    // This will contain text blocks, image URLs, code blocks, etc.
    @NotNull(message = "Content is required")
    private String content; // JSON string from frontend editor (max 5MB)
    
    private int displayOrder; // Display order within the topic
    
    private boolean isActive = true;
    
    private boolean isDraft = false; // Allow saving drafts
    
    private int estimatedReadTime; // In minutes (calculated based on content)
    
    private String createdBy; // Admin/SuperAdmin user ID
    
    private String updatedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime publishedAt; // When it was made public
    
    // Constructors
    public InterviewDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void generateSlug() {
        if (this.title != null) {
            this.slug = this.title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        }
    }
    
    public void publish() {
        this.isDraft = false;
        this.publishedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Calculate estimated read time based on content length
    public void calculateReadTime() {
        if (this.content != null) {
            // Rough estimation: 1 minute per 1000 characters
            int contentLength = this.content.length();
            this.estimatedReadTime = Math.max(1, contentLength / 1000);
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTopicId() {
        return topicId;
    }
    
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        generateSlug();
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        calculateReadTime();
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
    
    public boolean isDraft() {
        return isDraft;
    }
    
    public void setDraft(boolean draft) {
        isDraft = draft;
    }
    
    public int getEstimatedReadTime() {
        return estimatedReadTime;
    }
    
    public void setEstimatedReadTime(int estimatedReadTime) {
        this.estimatedReadTime = estimatedReadTime;
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
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}