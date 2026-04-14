package com.lz.common.aspect;

import com.lz.common.annotation.RequireEventAdmin;
import com.lz.common.annotation.RequireRole;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.entity.EventAdminMapping;
import com.lz.entity.User;
import com.lz.mapper.EventAdminMappingMapper;
import com.lz.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * 权限校验切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionAspect {

    private final UserMapper userMapper;
    private final EventAdminMappingMapper eventAdminMappingMapper;

    /**
     * 校验角色权限
     */
    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录", 401);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", 401);
        }

        boolean hasRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role == user.getUserType());

        if (!hasRole) {
            throw new BusinessException("权限不足", 403);
        }
    }

    /**
     * 校验赛事管理员权限
     * 需要从请求参数中获取 eventId，约定参数名为 eventId 或 id（如果是路径参数需额外处理，此处简化假设为 Query/Body 参数或 DTO）
     * 暂时只支持从 URL Query 或 Body 第一层字段获取，复杂场景需扩展
     */
    @Before("@annotation(requireEventAdmin)")
    public void checkEventAdmin(JoinPoint joinPoint, RequireEventAdmin requireEventAdmin) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录", 401);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", 401);
        }

        // 超级管理员直接放行
        if (user.getUserType() == UserRole.SCHOOL_ADMIN || user.getUserType() == UserRole.SUPER_ADMIN) {
            return;
        }

        // 获取 eventId
        Long eventId = resolveEventId(joinPoint);
        if (eventId == null) {
            // 如果无法解析 eventId，可能需要根据业务决定是否放行或报错
            // 这里为了安全，默认拦截
            log.warn("RequireEventAdmin: 无法解析 eventId");
            throw new BusinessException("无法验证赛事权限", 403);
        }

        // 校验映射关系
        EventAdminMapping mapping = eventAdminMappingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EventAdminMapping>()
                        .eq(EventAdminMapping::getEventId, eventId)
                        .eq(EventAdminMapping::getUserId, userId)
        );

        if (mapping == null) {
            throw new BusinessException("您不是该赛事的管理员", 403);
        }
    }

    private Long resolveEventId(JoinPoint joinPoint) {
        Long eventIdFromRequest = resolveEventIdFromRequest();
        if (eventIdFromRequest != null) {
            return eventIdFromRequest;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String parameterName = parameterNames != null && parameterNames.length > i ? parameterNames[i] : null;
            if ("eventId".equals(parameterName) && arg != null) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
                if (arg instanceof String str && str.matches("\\d+")) {
                    return Long.valueOf(str);
                }
            }
        }

        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof String str && str.matches("\\d+")) {
                return Long.valueOf(str);
            }
            if (arg != null) {
                for (String methodName : List.of("getEventId", "getEvent", "getId")) {
                    try {
                        java.lang.reflect.Method method = arg.getClass().getMethod(methodName);
                        Object result = method.invoke(arg);
                        if (result instanceof Long) {
                            return (Long) result;
                        }
                        if (result instanceof String value && value.matches("\\d+")) {
                            return Long.valueOf(value);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return resolveEventIdFromRequest();
    }

    private Long resolveEventIdFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String eventIdStr = request.getParameter("eventId");
            if (eventIdStr == null) {
                eventIdStr = request.getParameter("id");
            }
            if (eventIdStr != null && eventIdStr.matches("\\d+")) {
                return Long.valueOf(eventIdStr);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
