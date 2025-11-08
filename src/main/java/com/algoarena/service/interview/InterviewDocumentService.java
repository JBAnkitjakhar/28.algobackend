// src/main/java/com/algoarena/service/interview/InterviewDocumentService.java

package com.algoarena.service.interview;

import com.algoarena.dto.interview.InterviewDTO.*;
import com.algoarena.model.InterviewDocument;
import com.algoarena.repository.InterviewDocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewDocumentService {
    
    private static final int MAX_DOCUMENT_SIZE = 5 * 1024 * 1024; // 5MB in bytes
    
    @Autowired
    private InterviewDocumentRepository documentRepository;
    
    @Autowired
    private InterviewTopicService topicService;  // Same package
    
    @Autowired
    private InterviewImageService imageService;  // Same package
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Get documents for a topic (without content - for listing)
    public Page<DocumentSummaryResponse> getDocumentsByTopic(String topicId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InterviewDocument> documents = documentRepository.findByTopicIdWithoutContent(topicId, true, pageable);
        
        return documents.map(this::convertToSummaryResponse);
    }
    
    // Get all documents for admin (including inactive)
    public Page<DocumentSummaryResponse> getDocumentsByTopicForAdmin(String topicId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InterviewDocument> documents = documentRepository.findByTopicIdOrderByDisplayOrderAsc(topicId, pageable);
        
        return documents.map(this::convertToSummaryResponse);
    }
    
    // Get single document with full content (cached)
    @Cacheable(value = "interviewDocuments", key = "#id")
    public DocumentFullResponse getDocumentById(String id) {
        InterviewDocument document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        if (!document.isActive() && !document.isDraft()) {
            throw new RuntimeException("Document is not accessible");
        }
        
        return convertToFullResponse(document);
    }
    
    // Get document by topic and slug
    public DocumentFullResponse getDocumentBySlug(String topicId, String slug) {
        InterviewDocument document = documentRepository.findByTopicIdAndSlug(topicId, slug)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.isActive() && !document.isDraft()) {
            throw new RuntimeException("Document is not accessible");
        }
        
        return convertToFullResponse(document);
    }
    
    // Create new document (Admin only)
    @CacheEvict(value = {"interviewDocuments", "interviewTopics"}, allEntries = true)
    public DocumentFullResponse createDocument(DocumentRequest request, String userId) {
        // Validate document size
        validateDocumentSize(request.getContent());
        
        // Check if document with same title exists in topic
        if (documentRepository.existsByTopicIdAndTitleIgnoreCase(request.getTopicId(), request.getTitle())) {
            throw new RuntimeException("Document already exists with title: " + request.getTitle());
        }
        
        InterviewDocument document = new InterviewDocument();
        document.setTopicId(request.getTopicId());
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setContent(request.getContent());
        document.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        document.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        document.setDraft(request.getIsDraft() != null ? request.getIsDraft() : false);
        document.setCreatedBy(userId);
        document.setUpdatedBy(userId);
        
        if (!document.isDraft()) {
            document.publish();
        }
        
        InterviewDocument saved = documentRepository.save(document);
        
        // Update topic document count
        topicService.updateDocumentCount(request.getTopicId());
        
        return convertToFullResponse(saved);
    }
    
    // Update document (Admin only)
    @CacheEvict(value = {"interviewDocuments", "interviewTopics"}, allEntries = true)
    public DocumentFullResponse updateDocument(String id, DocumentRequest request, String userId) {
        // Validate document size
        validateDocumentSize(request.getContent());
        
        InterviewDocument document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        // Check if new title conflicts with another document
        if (!document.getTitle().equalsIgnoreCase(request.getTitle()) && 
            documentRepository.existsByTopicIdAndTitleIgnoreCase(document.getTopicId(), request.getTitle())) {
            throw new RuntimeException("Another document already exists with title: " + request.getTitle());
        }
        
        // Handle image cleanup if content changed
        Set<String> oldImages = extractImageUrls(document.getContent());
        Set<String> newImages = extractImageUrls(request.getContent());
        Set<String> imagesToDelete = new HashSet<>(oldImages);
        imagesToDelete.removeAll(newImages);
        
        // Delete removed images from Cloudinary
        for (String imageUrl : imagesToDelete) {
            imageService.deleteImageFromUrl(imageUrl);
        }
        
        // Update document fields
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setContent(request.getContent());
        
        if (request.getDisplayOrder() != null) {
            document.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsActive() != null) {
            document.setActive(request.getIsActive());
        }
        
        if (request.getIsDraft() != null) {
            document.setDraft(request.getIsDraft());
            if (!request.getIsDraft() && document.getPublishedAt() == null) {
                document.publish();
            }
        }
        
        document.setUpdatedBy(userId);
        document.setUpdatedAt(LocalDateTime.now());
        
        InterviewDocument saved = documentRepository.save(document);
        
        // Update topic document count if topic changed
        topicService.updateDocumentCount(document.getTopicId());
        
        return convertToFullResponse(saved);
    }
    
    // Delete document (Admin only)
    @CacheEvict(value = {"interviewDocuments", "interviewTopics"}, allEntries = true)
    public void deleteDocument(String id) {
        InterviewDocument document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        // Extract and delete all images from content
        Set<String> images = extractImageUrls(document.getContent());
        for (String imageUrl : images) {
            imageService.deleteImageFromUrl(imageUrl);
        }
        
        String topicId = document.getTopicId();
        documentRepository.deleteById(id);
        
        // Update topic document count
        topicService.updateDocumentCount(topicId);
    }
    
    // Validate document size (max 5MB)
    private void validateDocumentSize(String content) {
        if (content == null) {
            throw new RuntimeException("Document content cannot be null");
        }
        
        int sizeInBytes = content.getBytes(StandardCharsets.UTF_8).length;
        if (sizeInBytes > MAX_DOCUMENT_SIZE) {
            double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
            throw new RuntimeException(String.format("Document size (%.2f MB) exceeds maximum allowed size of 5MB", sizeInMB));
        }
    }
    
    // Extract image URLs from content JSON
    private Set<String> extractImageUrls(String content) {
        Set<String> urls = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return urls;
        }
        
        try {
            JsonNode root = objectMapper.readTree(content);
            extractImageUrlsFromNode(root, urls);
        } catch (Exception e) {
            // Log error but don't fail
            System.err.println("Error extracting image URLs: " + e.getMessage());
        }
        
        return urls;
    }
    
    // Recursive helper to extract image URLs from JSON
    private void extractImageUrlsFromNode(JsonNode node, Set<String> urls) {
        if (node.isArray()) {
            for (JsonNode item : node) {
                extractImageUrlsFromNode(item, urls);
            }
        } else if (node.isObject()) {
            // Check if this is an image block
            JsonNode typeNode = node.get("type");
            if (typeNode != null && "image".equals(typeNode.asText())) {
                // Look for URL in various possible locations
                JsonNode urlNode = node.get("url");
                if (urlNode == null) {
                    JsonNode imageMeta = node.get("imageMeta");
                    if (imageMeta != null) {
                        urlNode = imageMeta.get("url");
                    }
                }
                if (urlNode != null && urlNode.isTextual()) {
                    urls.add(urlNode.asText());
                }
            }
            
            // Recursively check all fields
            node.fields().forEachRemaining(entry -> {
                extractImageUrlsFromNode(entry.getValue(), urls);
            });
        }
    }
    
    // Convert to summary response (without content)
    private DocumentSummaryResponse convertToSummaryResponse(InterviewDocument document) {
        DocumentSummaryResponse response = new DocumentSummaryResponse();
        response.setId(document.getId());
        response.setTopicId(document.getTopicId());
        response.setTitle(document.getTitle());
        response.setSlug(document.getSlug());
        response.setDescription(document.getDescription());
        response.setDisplayOrder(document.getDisplayOrder());
        response.setActive(document.isActive());
        response.setDraft(document.isDraft());
        response.setEstimatedReadTime(document.getEstimatedReadTime());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setPublishedAt(document.getPublishedAt());
        return response;
    }
    
    // Convert to full response (with content)
    private DocumentFullResponse convertToFullResponse(InterviewDocument document) {
        DocumentFullResponse response = new DocumentFullResponse();
        response.setId(document.getId());
        response.setTopicId(document.getTopicId());
        response.setTitle(document.getTitle());
        response.setSlug(document.getSlug());
        response.setDescription(document.getDescription());
        response.setDisplayOrder(document.getDisplayOrder());
        response.setActive(document.isActive());
        response.setDraft(document.isDraft());
        response.setEstimatedReadTime(document.getEstimatedReadTime());
        response.setContent(document.getContent());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setPublishedAt(document.getPublishedAt());
        return response;
    }
}