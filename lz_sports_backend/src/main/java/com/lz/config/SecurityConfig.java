package com.lz.config;

import com.lz.common.exception.CustomAccessDeniedHandler;
import com.lz.common.exception.CustomAuthenticationEntryPoint;
import com.lz.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security Configuration
 * 核心职责：仅配置框架基础规则，放行/拦截逻辑由JwtAuthenticationFilter统一处理
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启@PreAuthorize注解支持
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * 公开路径列表（与JwtAuthenticationFilter保持一致）
     */
    private static final String[] PUBLIC_PATHS = {
            // 认证相关
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/api/auth/reset-password",
            // 系统初始化
            "/api/system/init-status",
            "/api/system/init",
            "/api/system/school-config",
            // 公共接口（支持子路径）
            "/api/public/**",
            // Swagger/Knife4j文档
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/favicon.ico"
    };

    /**
     * 单例 SecurityFilterChain：移除所有放行规则，仅保留基础配置
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
                // ========== 新增：配置异常处理器 ==========
                .exceptionHandling(exception -> exception
                        // 认证异常处理器（401）
                        .authenticationEntryPoint(authenticationEntryPoint)
                        // 授权异常处理器（403）
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // ===== 关键修改：配置请求授权 =====
                .authorizeHttpRequests(auth -> auth
                        // 1. 公开路径不需要认证
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // 2. 其他所有请求需要认证
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
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
