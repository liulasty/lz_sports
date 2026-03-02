package com.lz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // CORS is now handled in SecurityConfig.java
}
