package com.lz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

/**
 * Swagger/OpenAPI + Knife4j 配置类
 * 解决 doc.html 静态资源访问问题 + 保留原有 OpenAPI 配置
 */
@Configuration
@EnableKnife4j  // 开启 Knife4j 增强功能（核心：加载 doc.html 静态资源）
public class SwaggerConfig implements WebMvcConfigurer {

    // 保留你原有 OpenAPI 核心配置，仅优化格式
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LZ Sports API")
                        .version("1.0")
                        .description("Backend API for LZ Sports Management System")
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                        .contact(new Contact()
                                .name("LZ Sports Team")
                                .email("support@lzsports.com")));
    }

    /**
     * 新增：配置静态资源映射（解决 doc.html 找不到的关键）
     * 映射 Knife4j 的静态资源路径到 classpath 对应位置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 映射 doc.html 入口文件
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // 2. 映射 webjars 下的静态资源（Knife4j 前端依赖）
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 3. 映射 OpenAPI 接口文档的 JSON 数据（可选，确保接口文档数据能正常加载）
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/v3/api-docs/");
    }
}
