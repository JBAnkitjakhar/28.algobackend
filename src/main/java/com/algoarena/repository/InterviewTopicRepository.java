// src/main/java/com/algoarena/repository/InterviewTopicRepository.java

package com.algoarena.repository;

import com.algoarena.model.InterviewTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewTopicRepository extends MongoRepository<InterviewTopic, String> {
    
    // Find all active topics ordered by displayOrder
    List<InterviewTopic> findByIsActiveOrderByDisplayOrderAsc(boolean isActive);
    
    // Find topic by slug
    Optional<InterviewTopic> findBySlug(String slug);
    
    // Find topic by name (case insensitive)
    Optional<InterviewTopic> findByNameIgnoreCase(String name);
    
    // Check if topic exists by name (for validation)
    boolean existsByNameIgnoreCase(String name);
    
    // Find all topics ordered by displayOrder (including inactive for admin)
    List<InterviewTopic> findAllByOrderByDisplayOrderAsc();
    
    // Custom query to get topics with document count for admin
    @Query("{ }")
    List<InterviewTopic> findAllWithStats();
}