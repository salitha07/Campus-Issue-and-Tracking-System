package com.campus.issue_tracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This makes http://localhost:8080/uploads/image.jpg work
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
