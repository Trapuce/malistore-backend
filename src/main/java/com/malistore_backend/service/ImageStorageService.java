package com.malistore_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageStorageService {
    
    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Store an uploaded file and return the public URL
     */
    public String storeImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return public URL
        String publicUrl = baseUrl + "/images/" + filename;
        log.info("Image stored successfully: {}", publicUrl);
        
        return publicUrl;
    }
    
    /**
     * Store multiple images and return their URLs
     */
    public String[] storeImages(MultipartFile[] files) throws IOException {
        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = storeImage(files[i]);
        }
        return urls;
    }
    
    /**
     * Delete an image file
     */
    public boolean deleteImage(String imageUrl) {
        try {
            String filename = extractFilenameFromUrl(imageUrl);
            Path filePath = Paths.get(uploadDir, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting image: {}", imageUrl, e);
            return false;
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * Extract filename from URL
     */
    private String extractFilenameFromUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
    
    /**
     * Validate image file
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}



