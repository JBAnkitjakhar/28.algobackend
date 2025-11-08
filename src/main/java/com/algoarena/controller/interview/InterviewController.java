// src/main/java/com/algoarena/controller/interview/InterviewController.java

package com.algoarena.controller.interview;

import com.algoarena.dto.interview.InterviewDTO.*;
import com.algoarena.service.interview.InterviewTopicService;
import com.algoarena.service.interview.InterviewDocumentService;
import com.algoarena.service.interview.InterviewImageService;
import com.algoarena.service.auth.JwtService;  // Fixed import path
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/interview")  // Changed from /api/interview since /api is already in context-path
@CrossOrigin
public class InterviewController {
    
    @Autowired
    private InterviewTopicService topicService;
    
    @Autowired
    private InterviewDocumentService documentService;
    
    @Autowired
    private InterviewImageService imageService;
    
    @Autowired
    private JwtService jwtService;
    
    // ==================== PUBLIC ENDPOINTS (Authenticated Users) ====================
    
    // Get all active topics with document count
    @GetMapping("/topics")
    public ResponseEntity<?> getAllTopics() {
        try {
            List<TopicResponse> topics = topicService.getAllActiveTopics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topics", topics);
            response.put("total", topics.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Get topic by ID or slug
    @GetMapping("/topics/{identifier}")
    public ResponseEntity<?> getTopic(@PathVariable String identifier) {
        try {
            TopicResponse topic;
            // Check if identifier is a MongoDB ID (24 hex characters) or slug
            if (identifier.matches("[a-fA-F0-9]{24}")) {
                topic = topicService.getTopicById(identifier);
            } else {
                topic = topicService.getTopicBySlug(identifier);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Get documents for a topic (paginated, without content)
    @GetMapping("/topics/{topicId}/documents")
    public ResponseEntity<?> getDocumentsByTopic(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<DocumentSummaryResponse> documents = documentService.getDocumentsByTopic(topicId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documents", documents.getContent());
            response.put("currentPage", page);
            response.put("totalPages", documents.getTotalPages());
            response.put("totalElements", documents.getTotalElements());
            response.put("hasNext", documents.hasNext());
            response.put("hasPrevious", documents.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Get single document with full content
    @GetMapping("/documents/{id}")
    public ResponseEntity<?> getDocument(@PathVariable String id) {
        try {
            DocumentFullResponse document = documentService.getDocumentById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("document", document);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Get document by topic and slug
    @GetMapping("/topics/{topicId}/documents/slug/{slug}")
    public ResponseEntity<?> getDocumentBySlug(
            @PathVariable String topicId,
            @PathVariable String slug) {
        try {
            DocumentFullResponse document = documentService.getDocumentBySlug(topicId, slug);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("document", document);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    // Get all topics for admin (including inactive)
    @GetMapping("/admin/topics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> getAllTopicsForAdmin() {
        try {
            List<TopicResponse> topics = topicService.getAllTopicsForAdmin();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topics", topics);
            response.put("total", topics.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Create new topic
    @PostMapping("/admin/topics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> createTopic(
            @Valid @RequestBody TopicRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromToken(httpRequest);
            TopicResponse topic = topicService.createTopic(request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Topic created successfully");
            response.put("topic", topic);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Update topic
    @PutMapping("/admin/topics/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> updateTopic(
            @PathVariable String id,
            @Valid @RequestBody TopicRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromToken(httpRequest);
            TopicResponse topic = topicService.updateTopic(id, request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Topic updated successfully");
            response.put("topic", topic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Delete topic
    @DeleteMapping("/admin/topics/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> deleteTopic(@PathVariable String id) {
        try {
            topicService.deleteTopic(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Topic deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Get documents for admin (including inactive)
    @GetMapping("/admin/topics/{topicId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> getDocumentsForAdmin(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<DocumentSummaryResponse> documents = documentService.getDocumentsByTopicForAdmin(topicId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documents", documents.getContent());
            response.put("currentPage", page);
            response.put("totalPages", documents.getTotalPages());
            response.put("totalElements", documents.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Create new document
    @PostMapping("/admin/documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> createDocument(
            @Valid @RequestBody DocumentRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromToken(httpRequest);
            DocumentFullResponse document = documentService.createDocument(request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document created successfully");
            response.put("document", document);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Update document
    @PutMapping("/admin/documents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> updateDocument(
            @PathVariable String id,
            @Valid @RequestBody DocumentRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromToken(httpRequest);
            DocumentFullResponse document = documentService.updateDocument(id, request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document updated successfully");
            response.put("document", document);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Delete document
    @DeleteMapping("/admin/documents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {
        try {
            documentService.deleteDocument(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Upload image
    @PostMapping("/admin/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "documents") String folder) {
        try {
            Map<String, Object> uploadResult = imageService.uploadImage(file, folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("image", uploadResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    // Helper method to extract user ID from JWT token
    private String extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);  // Now this method exists!
        }
        throw new RuntimeException("Invalid authentication token");
    }
    
    // Helper method to create error response
    private ResponseEntity<?> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}