package com.palak.expensemanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowed = System.getenv("ALLOWED_ORIGINS");
        if (allowed == null || allowed.isBlank()) {
            // default to allow all origins for ease of testing; change for production
            allowed = "*";
        }
        String[] origins = allowed.equals("*") ? new String[] {"*"} : allowed.split(",");

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
