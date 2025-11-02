package com.malistore_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images
        Path uploadPath = Paths.get(uploadDir);
        String uploadPathString = uploadPath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPathString + "/");
    }
}




