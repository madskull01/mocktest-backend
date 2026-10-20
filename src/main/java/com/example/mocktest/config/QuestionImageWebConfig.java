package com.example.mocktest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class QuestionImageWebConfig implements WebMvcConfigurer {

    private final Path imageDirectory;

    public QuestionImageWebConfig(
            @Value("${app.uploads.question-images-dir:uploads/question-images}")
            String imageDirectory) {
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = imageDirectory.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        registry.addResourceHandler("/uploads/question-images/**")
                .addResourceLocations(resourceLocation);
    }
}
