// src/main/java/com/algoarena/service/interview/InterviewImageService.java

package com.algoarena.service.interview;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.algoarena.config.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InterviewImageService {
    
    private static final long MAX_IMAGE_SIZE = 500 * 1024; // 500KB in bytes
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    
    private final Cloudinary cloudinary;
    
    @Autowired
    public InterviewImageService(CloudinaryConfig cloudinaryConfig) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryConfig.getCloudName());
        config.put("api_key", cloudinaryConfig.getApiKey());
        config.put("api_secret", cloudinaryConfig.getApiSecret());
        this.cloudinary = new Cloudinary(config);
    }
    
    // Upload image for interview documents
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file size
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException(String.format("Image size exceeds maximum allowed size of 500KB. Current size: %d KB", 
                file.getSize() / 1024));
        }
        
        // Validate file type
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new RuntimeException("Invalid image type. Allowed types: JPEG, JPG, PNG, GIF, WEBP");
        }
        
        // Upload to Cloudinary - Fixed type safety
        Map<String, Object> uploadParams = new HashMap<>();
        uploadParams.put("folder", "algoarena/interview/" + folder);
        uploadParams.put("resource_type", "image");
        uploadParams.put("allowed_formats", "jpg,jpeg,png,gif,webp");
        uploadParams.put("max_file_size", MAX_IMAGE_SIZE);
        
        Map<String, Object> transformation = new HashMap<>();
        transformation.put("quality", "auto:good");
        transformation.put("fetch_format", "auto");
        uploadParams.put("transformation", transformation);
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("url", uploadResult.get("secure_url"));
        response.put("publicId", uploadResult.get("public_id"));
        response.put("format", uploadResult.get("format"));
        response.put("width", uploadResult.get("width"));
        response.put("height", uploadResult.get("height"));
        response.put("size", uploadResult.get("bytes"));
        response.put("sizeKB", ((Number) uploadResult.get("bytes")).longValue() / 1024.0);
        
        return response;
    }
    
    // Delete image by public ID
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Log error but don't fail the operation
            System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
        }
    }
    
    // Delete image by URL (extract public ID first)
    public void deleteImageFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary")) {
            return; // Not a Cloudinary URL
        }
        
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId != null) {
            deleteImage(publicId);
        }
    }
    
    // Extract public ID from Cloudinary URL
    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // Pattern to extract public ID from Cloudinary URL
            // Example: https://res.cloudinary.com/demo/image/upload/v1234/folder/filename.jpg
            Pattern pattern = Pattern.compile("/v\\d+/(.+?)\\.[^.]+$");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Alternative pattern without version
            pattern = Pattern.compile("/upload/(.+?)\\.[^.]+$");
            matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            System.err.println("Error extracting public ID from URL: " + e.getMessage());
        }
        
        return null;
    }
    
    // Validate image URL
    public boolean isValidCloudinaryUrl(String url) {
        return url != null && url.contains("cloudinary.com") && url.startsWith("https://");
    }
}