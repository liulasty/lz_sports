package com.lz.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.common.result.Result;
import com.lz.common.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证异常处理器（401）
 * 处理：无token、token无效、token过期等认证失败情况
 * 注意：JwtAuthenticationFilter中已经处理了大部分认证异常，这个作为补充和兜底
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String clientIp = getClientIp(request);

        log.warn("[Security认证异常] IP: {}, URI: {}, 原因: {}",
                clientIp, requestURI, authException.getMessage());

        writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                Result.error(ResultCode.UNAUTHORIZED, "未登录或token已失效"));
    }

    /**
     * 统一响应写入方法
     */
    private void writeResponse(HttpServletResponse response, int httpStatus, Result<?> result)
            throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 获取客户端真实IP
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
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}