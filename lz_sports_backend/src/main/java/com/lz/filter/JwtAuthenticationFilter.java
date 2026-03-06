package com.lz.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.common.context.BaseContext;
import com.lz.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter
 * @author Administrator
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.key:lz_sports_secret_key}")
    private String jwtKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (JwtUtil.isExpired(token, jwtKey)) {
                throw new RuntimeException("Token has expired");
            }
            Map<String, Object> claims = JwtUtil.parseToken(token, jwtKey);
            Long userId = Long.valueOf(claims.get("id").toString());
            BaseContext.setCurrentId(userId);

            List<GrantedAuthority> authorities = new ArrayList<>();
            Object roleObj = claims.get("role");
            if (roleObj != null) {
                String role = roleObj.toString();
                if ("管理员".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                } else if ("运动员".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ATHLETE"));
                }
            }
            authorities.add(new SimpleGrantedAuthority("USER"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());

            // --- 安全性改进 ---
            // 不再调用 filterChain.doFilter，而是直接返回错误响应
            response.setStatus(HttpStatus.OK.value()); // 前端根据 code 判断，或者用 401 也可以，这里保持 RESTful 风格返回 200 + code 401 ? 用户要求 401 code.
            // 用户要求：401 未登录 token 不存在或已过期。
            // 通常 Result 结构是 JSON，HTTP Status 可以是 200。
            // 但为了更好兼容，如果返回 JSON 含 code=401，HTTP status 也可以是 200。
            // 不过 Axios 拦截器里写的是 `if (res.code !== 1)` (旧) -> 新逻辑需适配。
            // 让我们保持 HTTP 200，内容 code 401。
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            // 手动构建 JSON 字符串，或者使用 ObjectMapper 序列化 Result 对象
            // Map.of("code", 401, "msg", "未登录", "data", null)
            new ObjectMapper().writeValue(response.getWriter(), com.lz.common.result.Result.error(com.lz.common.result.ResultCode.UNAUTHORIZED));
            return;
            // --- 结束改进 ---
        }

        filterChain.doFilter(request, response);
        BaseContext.removeCurrentId(); // Clear context after request
    }
}
