package com.lz.Interceptor;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.config.AppConfig;
import com.lz.pojo.error.ErrorResponse;
import com.lz.utils.JwtUtil;
import com.lz.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * JWT 令牌管理拦截器
 *
 * @author lz
 * @date 2023/11/08
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ObjectMapper objectMapper; 

    /**
     * 校验jwt
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     *
     * @return boolean
     *
     * @throws Exception 例外
     */
    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,@Nonnull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            // 直接放行非Controller方法的请求
            return true; 
        }

        String token = request.getHeader("jwt");
        // 注意：生产环境中需评估是否记录JWT
        log.info("jwt校验:{}", token); 

        try {
            Map<String, Object> map = JwtUtil.parseToken(token, appConfig.getJwtKey());
            Integer id = (Integer) map.get("id");
            log.info("当前用户id：{}", id);
            String username = (String) map.get("username");
            log.info("当前用户：{}", username);
            BaseContext.setCurrentId(((long) id));
            // 通过验证，放行
            return true; 
        } catch (Exception ex) {
            // 将错误处理逻辑封装至独立方法
            handleJwtException(response, ex); 
            return false;
        }
    }

    /**
     * 处理JWT验证异常
     */
    private void handleJwtException(HttpServletResponse response, Exception ex) throws IOException {
        // 使用日志框架记录错误，避免使用printStackTrace()
        log.error("校验失败", ex); 

        ErrorResponse errorResponse = new ErrorResponse();
        // 使用401未授权状态码
        errorResponse.setStatus(401); 
        errorResponse.setCode("1");
        errorResponse.setMessage("令牌校验失败");
        errorResponse.setTimestamp(System.currentTimeMillis());
        // 设置响应状态码
        response.setStatus(401); 
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            // 使用注入的objectMapper
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse)); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.getWriter().flush();
    }
}