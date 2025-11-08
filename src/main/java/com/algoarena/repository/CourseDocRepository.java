// src/main/java/com/algoarena/repository/CourseDocRepository.java
package com.algoarena.repository;

import com.algoarena.model.CourseDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseDocRepository extends MongoRepository<CourseDoc, String> {

    // Find all documents in a topic (ordered by display order)
    List<CourseDoc> findByTopic_IdOrderByDisplayOrderAsc(String topicId);

    // Find all documents in a topic (ordered by creation date)
    List<CourseDoc> findByTopic_IdOrderByCreatedAtDesc(String topicId);

    // Find document by title and topic (case-insensitive)
    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'topic.$id': ?1 }")
    Optional<CourseDoc> findByTitleAndTopicId(String title, String topicId);

    // Check if document title exists in topic (case-insensitive)
    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'topic.$id': ?1 }")
    boolean existsByTitleAndTopicId(String title, String topicId);

    // Count documents in a topic
    long countByTopic_Id(String topicId);

    // Find documents by creator
    List<CourseDoc> findByCreatedBy_Id(String createdById);

    // Count total documents
    long count();

    // Find documents exceeding size limit
    @Query("{ 'totalSize': { $gt: ?0 } }")
    List<CourseDoc> findDocumentsExceedingSize(Long maxSize);
}