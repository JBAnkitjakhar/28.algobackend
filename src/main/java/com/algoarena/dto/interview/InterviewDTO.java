// src/main/java/com/algoarena/dto/interview/InterviewDTO.java

package com.algoarena.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

public class InterviewDTO {
    
    // ==================== Request DTOs ====================
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopicRequest {
        @NotBlank(message = "Topic name is required")
        @Size(min = 2, max = 100, message = "Topic name must be between 2 and 100 characters")
        private String name;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        private String icon;
        private String color;
        private Integer displayOrder;
        private Boolean isActive;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentRequest {
        @NotBlank(message = "Document title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        private String title;
        
        @NotNull(message = "Topic ID is required")
        private String topicId;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        @NotNull(message = "Content is required")
        private String content; // JSON string from frontend
        
        private Integer displayOrder;
        private Boolean isActive;
        private Boolean isDraft;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getTopicId() { return topicId; }
        public void setTopicId(String topicId) { this.topicId = topicId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Boolean getIsDraft() { return isDraft; }
        public void setIsDraft(Boolean isDraft) { this.isDraft = isDraft; }
    }
    
    // ==================== Response DTOs ====================
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopicResponse {
        private String id;
        private String name;
        private String slug;
        private String description;
        private String icon;
        private String color;
        private int displayOrder;
        private int documentCount;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Constructor
        public TopicResponse() {}
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
        
        public int getDocumentCount() { return documentCount; }
        public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentSummaryResponse {
        private String id;
        private String topicId;
        private String title;
        private String slug;
        private String description;
        private int displayOrder;
        private boolean isActive;
        private boolean isDraft;
        private int estimatedReadTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;
        
        // Constructor
        public DocumentSummaryResponse() {}
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTopicId() { return topicId; }
        public void setTopicId(String topicId) { this.topicId = topicId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public boolean isDraft() { return isDraft; }
        public void setDraft(boolean draft) { isDraft = draft; }
        
        public int getEstimatedReadTime() { return estimatedReadTime; }
        public void setEstimatedReadTime(int estimatedReadTime) { this.estimatedReadTime = estimatedReadTime; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentFullResponse extends DocumentSummaryResponse {
        private String content; // Full content JSON string
        
        // Constructor
        public DocumentFullResponse() {}
        
        // Getter and Setter for content
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}