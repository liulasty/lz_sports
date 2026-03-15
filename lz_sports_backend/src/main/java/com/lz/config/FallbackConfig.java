package com.lz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 兜底配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "fallback.images")
public class FallbackConfig {
    
    /**
     * 兜底图片URL列表
     */
    private List<String> urls;
}
