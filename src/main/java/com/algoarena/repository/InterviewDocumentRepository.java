// src/main/java/com/algoarena/repository/InterviewDocumentRepository.java

package com.algoarena.repository;

import com.algoarena.model.InterviewDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewDocumentRepository extends MongoRepository<InterviewDocument, String> {
    
    // Find all documents for a topic (paginated)
    Page<InterviewDocument> findByTopicIdAndIsActiveOrderByDisplayOrderAsc(
        String topicId, boolean isActive, Pageable pageable);
    
    // Find all documents for a topic (no pagination)
    List<InterviewDocument> findByTopicIdAndIsActiveOrderByDisplayOrderAsc(
        String topicId, boolean isActive);
    
    // Find all documents for a topic (including inactive for admin)
    Page<InterviewDocument> findByTopicIdOrderByDisplayOrderAsc(
        String topicId, Pageable pageable);
    
    // Count documents for a topic
    long countByTopicIdAndIsActive(String topicId, boolean isActive);
    
    // Count all documents for a topic (including inactive)
    long countByTopicId(String topicId);
    
    // Find document by slug within a topic
    Optional<InterviewDocument> findByTopicIdAndSlug(String topicId, String slug);
    
    // Check if document title exists within a topic
    boolean existsByTopicIdAndTitleIgnoreCase(String topicId, String title);
    
    // Delete all documents for a topic
    void deleteByTopicId(String topicId);
    
    // Find documents with only basic info (for listing)
    @Query(value = "{ 'topicId': ?0, 'isActive': ?1 }", 
           fields = "{ 'content': 0 }") // Exclude content field
    Page<InterviewDocument> findByTopicIdWithoutContent(
        String topicId, boolean isActive, Pageable pageable);
    
    // Get all documents with specific fields for admin
    @Query(value = "{ }", 
           fields = "{ 'title': 1, 'topicId': 1, 'isActive': 1, 'isDraft': 1, 'createdAt': 1 }")
    List<InterviewDocument> findAllBasicInfo();
}