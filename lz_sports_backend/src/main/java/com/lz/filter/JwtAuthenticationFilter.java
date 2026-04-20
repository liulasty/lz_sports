package com.lz.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.common.context.BaseContext;
import com.lz.common.result.Result;
import com.lz.common.result.ResultCode;
import com.lz.util.JwtUtil;
import com.lz.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.lz.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter
 * 核心职责：统一处理token校验、放行/拦截逻辑，SecurityConfig仅保留基础配置
 * @author Administrator
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.key:lz_sports_secret_key}")
    private String jwtKey;

    @Resource
    private RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 统一维护公开路径（无需token），覆盖所有需要放行的场景
    private static final List<String> PUBLIC_PATHS = List.of(
            // 认证相关
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/api/auth/reset-password",
            "/api/auth/send-verify-code",
            // 系统初始化
            "/api/system/init-status",
            "/api/system/init",
            "/api/system/school-config",
            // 公共接口（支持子路径）
            "/api/public/",
            // 公开成绩榜（仅 GET /api/score/public/{eventId}）
            "/api/score/public/",
            // Swagger/Knife4j文档
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/swagger-resources/",
            "/swagger-ui/",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal( @NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String clientIp = getClientIp(request);
        String requestMethod = request.getMethod();

        try {
            // 1. 公开路径直接放行（收敛所有放行逻辑到这里）
            if (isPublicPath(requestURI)) {
                log.info("[JWT过滤器] 公开路径放行 - IP: {}, 方法: {}, URI: {}", clientIp, requestMethod, requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 检查token是否存在
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("[JWT过滤器] 无token拦截 - IP: {}, 方法: {}, URI: {}", clientIp, requestMethod, requestURI);
                writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        Result.error(ResultCode.UNAUTHORIZED, "请先登录"));
                return;
            }

            // 3. 验证token并设置认证信息
            processToken(token, clientIp, requestURI);

            // 4. 继续过滤器链
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("[JWT过滤器] Token验证失败 - IP: {}, URI: {}, 错误: {}", clientIp, requestURI, e.getMessage(), e);
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录"));
        } finally {
            // 确保清理ThreadLocal，防止内存泄漏
            BaseContext.removeCurrentId();
        }
    }

    /**
     * 处理token并设置认证信息
     */
    private void processToken(String token, String clientIp, String requestURI) {
        // 验证token是否过期（统一中文异常信息）
        if (JwtUtil.isExpired(token, jwtKey)) {
            throw new RuntimeException("Token已过期");
        }

        // 解析token
        Map<String, Object> claims = JwtUtil.parseToken(token, jwtKey);
        Long userId = Long.valueOf(claims.get("id").toString());
        Object latestTokenObj = redisUtil.get("auth:token:" + userId);
        if (latestTokenObj == null || !token.equals(latestTokenObj.toString())) {
            throw new RuntimeException("Token已失效");
        }

        // 设置用户上下文
        BaseContext.setCurrentId(userId);

        // 构建权限列表
        List<GrantedAuthority> authorities = buildAuthorities(claims);

        // 设置Spring Security认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 升级日志级别为INFO，生产环境可追踪
        log.info("[JWT过滤器] Token验证成功 - IP: {}, 用户ID: {}, URI: {},authorities:{}", clientIp, userId, requestURI, authorities);
    }

    /**
     * 构建用户权限（规范ROLE_前缀，符合Spring Security最佳实践）
     */
    private List<GrantedAuthority> buildAuthorities(Map<String, Object> claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加基础用户权限
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 添加角色权限（如果有）
        Object roleObj = claims.get("role");
        if (roleObj != null) {
            String role = roleObj.toString().trim();
            if (!role.isEmpty()) {
                String normalizedRole = role.toUpperCase();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
                if ("SUPER_ADMIN".equals(normalizedRole)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SCHOOL_ADMIN"));
                } else if ("管理员".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SCHOOL_ADMIN"));
                } else if ("运动员".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ATHLETE"));
                }
            }
        }

        return authorities;
    }

    /**
     * 判断是否为公开路径（修复路径匹配问题，兼容有无末尾斜杠）
     */
    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(path -> {
            // 精确匹配（如/doc.html）
            if (requestURI.equals(path)) {
                return true;
            }
            // 前缀匹配（如/api/public/开头的所有路径，兼容/api/public和/api/public/xxx）
            if (path.endsWith("/") && requestURI.startsWith(path)) {
                return true;
            }
            // 兼容无末尾斜杠的前缀匹配（如/api/public匹配/api/public/xxx）
            if (!path.endsWith("/") && requestURI.startsWith(path + "/")) {
                return true;
            }
            return false;
        });
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 统一响应写入方法（保持原有逻辑）
     */
    private void writeResponse(HttpServletResponse response, int httpStatus, Result<?> result)
            throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 获取客户端真实IP（优化IP获取逻辑，兼容更多代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个真实IP
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
