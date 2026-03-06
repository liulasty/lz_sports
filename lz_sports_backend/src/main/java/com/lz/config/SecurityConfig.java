package com.lz.config;

import com.lz.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security Configuration
 * 整合所有 Security 规则，解决 Knife4j 403 问题
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 单例 SecurityFilterChain：整合所有放行规则，避免多 Bean 冲突
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭 CSRF（前后端分离项目推荐）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话（JWT 认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 授权规则配置（核心：整合所有放行路径）
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行 Knife4j/Swagger 所有相关路径（解决 403 关键）
                        .requestMatchers(
                                "/doc.html",          // Knife4j 主页面
                                "/webjars/**",        // Knife4j 前端静态资源（CSS/JS/图片）
                                "/v3/api-docs/**",    // OpenAPI 接口文档数据
                                "/swagger-resources/**", // Swagger 资源配置
                                "/swagger-ui/**",      // Swagger UI 备用路径
                                "/favicon.ico"
                        ).permitAll()
                        // 2. 放行业务接口（登录/注册/初始化/验证码）
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/send-code",
                                "/api/auth/verify-code",
                                "/api/auth/reset-password",
                                "/api/system/init-status",
                                "/api/system/init",
                                "/api/public/**"
                        ).permitAll()
                        // 3. 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器（在用户名密码过滤器之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 跨域配置（保持原有配置不变）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有域名（生产环境建议指定具体域名）
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 允许携带凭证（Cookie）
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}