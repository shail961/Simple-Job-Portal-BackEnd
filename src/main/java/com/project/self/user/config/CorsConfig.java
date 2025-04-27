package com.project.self.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 1. Create a CorsConfiguration object
        CorsConfiguration config = new CorsConfiguration();

        // 2. Set allowed origins
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));  // <-- your React frontend

        // 3. Set allowed HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 4. Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // 5. Allow credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // 6. Create a source and register the config to apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // Apply config to ALL routes

        return source;
    }
}