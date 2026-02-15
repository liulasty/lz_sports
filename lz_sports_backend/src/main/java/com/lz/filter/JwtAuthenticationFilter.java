package com.lz.filter;

import com.lz.common.context.BaseContext;
import com.lz.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter
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
            // Don't fail here, let Security check permissions
        }

        filterChain.doFilter(request, response);
        BaseContext.removeCurrentId(); // Clear context after request
    }
}
