package com.lz.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lz
 */
@Configuration
public class AppConfig {
    @Value("${jwt.KEY}")
    private String jwtKey;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Bean
    public String getJwtKey() {
        
        return jwtKey;
    }
}