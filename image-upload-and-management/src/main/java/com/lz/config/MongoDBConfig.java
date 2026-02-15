package com.lz.config;

/*
 * Created with IntelliJ IDEA.
 * @Author: lz
 * @Date: 2024/03/23/17:31
 * @Description:
 */

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;

/**
 * mongo dbconfig
 *
 * @author lz
 * @date 2024/03/23
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.data.mongodb", name = "enabled",
        havingValue = "false")
public class MongoDBConfig {
    @Autowired
    private org.springframework.core.env.Environment env;

    
    
    
    @Bean
    public MongoClient mongoClient() {
        String uri = env.getProperty("spring.data.mongodb.uri");
        if (!StringUtils.hasText(uri)) {
            throw new IllegalStateException("MongoDB URI 配置不正确。");
        }
        try {
            return MongoClients.create(uri);
        } catch (Exception e) {
            throw new RuntimeException("无法创建 MongoDB 客户端。", e);
        }
    }

    @ConditionalOnProperty(prefix = "spring.data.mongodb", name = "enabled", havingValue = "true")
    @Bean
    public MongoTemplate mongoTemplate() {
        String database = env.getProperty("spring.data.mongodb.database");
        if (!StringUtils.hasText(database)) {
            throw new IllegalStateException("MongoDB 数据库名称配置不正确。");
        }
        try {
            return new MongoTemplate(mongoClient(), database);
        } catch (Exception e) {
            throw new RuntimeException("初始化 MongoDB 模板失败.", e);
        }
    }

    @PreDestroy
    public void closeMongoClient() {
        // 可能为 null 如果未启用
        MongoClient mongoClient = this.mongoClient(); 
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}