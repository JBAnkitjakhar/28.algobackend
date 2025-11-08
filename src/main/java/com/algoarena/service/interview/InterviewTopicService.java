// src/main/java/com/algoarena/service/interview/InterviewTopicService.java

package com.algoarena.service.interview;

import com.algoarena.dto.interview.InterviewDTO.*;
import com.algoarena.model.InterviewTopic;
import com.algoarena.repository.InterviewTopicRepository;
import com.algoarena.repository.InterviewDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewTopicService {
    
    @Autowired
    private InterviewTopicRepository topicRepository;
    
    @Autowired
    private InterviewDocumentRepository documentRepository;
    
    @Autowired
    private InterviewImageService imageService;  // Same package, no issue
    
    // Get all active topics for users (cached)
    @Cacheable(value = "interviewTopics", key = "'active'")
    public List<TopicResponse> getAllActiveTopics() {
        List<InterviewTopic> topics = topicRepository.findByIsActiveOrderByDisplayOrderAsc(true);
        return topics.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    // Get all topics for admin (including inactive)
    public List<TopicResponse> getAllTopicsForAdmin() {
        List<InterviewTopic> topics = topicRepository.findAllByOrderByDisplayOrderAsc();
        return topics.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    // Get topic by ID
    public TopicResponse getTopicById(String id) {
        InterviewTopic topic = topicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
        return convertToResponse(topic);
    }
    
    // Get topic by slug
    public TopicResponse getTopicBySlug(String slug) {
        InterviewTopic topic = topicRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Topic not found with slug: " + slug));
        return convertToResponse(topic);
    }
    
    // Create new topic (Admin only)
    @CacheEvict(value = "interviewTopics", allEntries = true)
    public TopicResponse createTopic(TopicRequest request, String userId) {
        // Check if topic already exists
        if (topicRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Topic already exists with name: " + request.getName());
        }
        
        InterviewTopic topic = new InterviewTopic();
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setIcon(request.getIcon());
        topic.setColor(request.getColor());
        topic.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        topic.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        topic.setCreatedBy(userId);
        topic.setUpdatedBy(userId);
        
        InterviewTopic saved = topicRepository.save(topic);
        return convertToResponse(saved);
    }
    
    // Update topic (Admin only)
    @CacheEvict(value = "interviewTopics", allEntries = true)
    public TopicResponse updateTopic(String id, TopicRequest request, String userId) {
        InterviewTopic topic = topicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
        
        // Check if new name conflicts with another topic
        if (!topic.getName().equalsIgnoreCase(request.getName()) && 
            topicRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Another topic already exists with name: " + request.getName());
        }
        
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        
        // Handle icon update (delete old from Cloudinary if changed)
        if (request.getIcon() != null && !request.getIcon().equals(topic.getIcon())) {
            if (topic.getIcon() != null) {
                imageService.deleteImageFromUrl(topic.getIcon());
            }
            topic.setIcon(request.getIcon());
        }
        
        topic.setColor(request.getColor());
        
        if (request.getDisplayOrder() != null) {
            topic.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsActive() != null) {
            topic.setActive(request.getIsActive());
        }
        
        topic.setUpdatedBy(userId);
        topic.setUpdatedAt(LocalDateTime.now());
        
        InterviewTopic saved = topicRepository.save(topic);
        return convertToResponse(saved);
    }
    
    // Delete topic (Admin only)
    @CacheEvict(value = "interviewTopics", allEntries = true)
    public void deleteTopic(String id) {
        InterviewTopic topic = topicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
        
        // Check if topic has documents
        long documentCount = documentRepository.countByTopicId(id);
        if (documentCount > 0) {
            throw new RuntimeException("Cannot delete topic with existing documents. Delete all documents first.");
        }
        
        // Delete icon from Cloudinary if exists
        if (topic.getIcon() != null) {
            imageService.deleteImageFromUrl(topic.getIcon());
        }
        
        topicRepository.deleteById(id);
    }
    
    // Update document count for a topic
    public void updateDocumentCount(String topicId) {
        InterviewTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        long count = documentRepository.countByTopicIdAndIsActive(topicId, true);
        topic.setDocumentCount((int) count);
        topicRepository.save(topic);
    }
    
    // Convert entity to response DTO
    private TopicResponse convertToResponse(InterviewTopic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setName(topic.getName());
        response.setSlug(topic.getSlug());
        response.setDescription(topic.getDescription());
        response.setIcon(topic.getIcon());
        response.setColor(topic.getColor());
        response.setDisplayOrder(topic.getDisplayOrder());
        response.setDocumentCount(topic.getDocumentCount());
        response.setActive(topic.isActive());
        response.setCreatedAt(topic.getCreatedAt());
        response.setUpdatedAt(topic.getUpdatedAt());
        return response;
    }
}