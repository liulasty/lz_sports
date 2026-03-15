package com.lz.util;



import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;


import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 权限注解解析工具类
 */
public class PreAuthorizeParserUtil {

    // 匹配hasAuthority('xxx')格式的正则表达式
    private static final Pattern AUTH_PATTERN = Pattern.compile("hasAuthority\\('([^']+)'\\)");

    /**
     * 从当前请求的方法中提取@PreAuthorize要求的权限
     * @return 权限字符串（如ROLE_SCHOOL_ADMIN），无则返回null
     */
    public static String getRequiredAuthority() {
        // 1. 获取当前请求的处理器方法
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object handler = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler");

        if (!(handler instanceof HandlerMethod)) {
            return null;
        }

        // 2. 提取@PreAuthorize注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        if (preAuthorize == null) {
            // 也检查类上的注解
            preAuthorize = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
            if (preAuthorize == null) {
                return null;
            }
        }

        // 3. 解析注解中的权限表达式（匹配hasAuthority('xxx')）
        String expression = preAuthorize.value();
        Matcher matcher = AUTH_PATTERN.matcher(expression);
        if (matcher.find()) {
            return matcher.group(1); // 返回匹配到的权限值（如ROLE_SCHOOL_ADMIN）
        }
        return null;
    }
}