package com.lz.common.exception;

import com.lz.common.result.Result;
import com.lz.common.result.ResultCode;
import com.lz.util.PreAuthorizeParserUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Form Data Validation Exception
     */
    @ExceptionHandler(BindException.class)
    public Result<String> bindExceptionHandler(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> messages = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return Result.error(ResultCode.PARAM_ERROR, messages.toString());
    }

    /**
     * Handle JSON Body Validation Exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> messages = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return Result.error(ResultCode.PARAM_ERROR, messages.toString());
    }

    /**
     * Handle Single Parameter Validation Exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> constraintViolationExceptionHandler(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> messages = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return Result.error(ResultCode.PARAM_ERROR, messages.toString());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> sqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error("SQL Integrity Constraint Violation: ", e);
        return Result.error(ResultCode.CONFLICT, "Database constraint violation");
    }

    @ExceptionHandler(SQLException.class)
    public Result<String> sqlExceptionHandler(SQLException e) {
        log.error("SQL Exception: ", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<String> businessExceptionHandler(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> accessDeniedException(AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());

        // 1. 提取当前接口要求的权限
        String requiredAuthority = PreAuthorizeParserUtil.getRequiredAuthority();

        // 2. 拼接精准提示
        String message;
        if (requiredAuthority != null) {
            message = String.format("无[%s]权限，无法操作", requiredAuthority);
        } else {
            // 兜底提示
            message = "无权限操作";
        }

        // 3. 返回带权限信息的结果
        return Result.error(ResultCode.FORBIDDEN, message);
    }

    // 处理静态资源未找到异常（过滤 Knife4j 和 favicon 相关路径）
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFound(NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();

        // 忽略 Knife4j 过时路径和 favicon.ico 错误
        if (resourcePath.contains("swagger-resources") || resourcePath.equals("favicon.ico")) {
            // 返回 404 但不打印自定义错误，避免干扰
            return Result.error(ResultCode.NOT_FOUND, "Resource not found");
        }

        return Result.error(ResultCode.NOT_FOUND, "Internal Server Error: No static resource " + resourcePath + ".");
    }
    
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception e) {
        log.error("Global Exception: ", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
