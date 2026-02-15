package com.lz.config;

/*
 * Created with IntelliJ IDEA.
 * @Author: lz
 * @Date: 2024/03/22/20:49
 * @Description:
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Redis 配置
 * 解决乱码问题
 * @author lz
 * @date 2024/03/23
 */
@Component
public class RedisConfig {
    @Autowired
    private RedisConnectionFactory factory;

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        configureKeySerializer(template);
        configureValueSerializer(template);
        configureHashKeyAndValueSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 Key 的序列化器
     */
    private void configureKeySerializer(RedisTemplate<String, Object> template) {
        template.setKeySerializer(new StringRedisSerializer());
    }

    /**
     * 配置 Value 的序列化器为 JSON 序列化器
     */
    private void configureValueSerializer(RedisTemplate<String, Object> template) {
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 为 Jackson 配置模块，以支持特定的类或特性，例如日期时间的处理
        SimpleModule module = new SimpleModule();
        /*
          注册一个模块到ObjectMapper中。
          这个方法通过添加对特定类型（例如DateTimeLiteralExpression）的序列化器，来定制JSON序列化和反序列化行为。
         
          @param module 要注册到ObjectMapper的模块。这个模块包含了对于特定类型序列化器的定义。
         * @return void 方法没有返回值。
         */
        
        objectMapper.registerModule(module);
        valueSerializer.setObjectMapper(objectMapper);
        template.setValueSerializer(valueSerializer);
    }

    /**
     * 配置 Hash Key 和 Hash Value 的序列化器
     */
    private void configureHashKeyAndValueSerializer(RedisTemplate<String, Object> template) {
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    }
}