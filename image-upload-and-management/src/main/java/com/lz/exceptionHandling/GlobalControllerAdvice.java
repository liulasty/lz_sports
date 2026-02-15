package com.lz.exceptionHandling;

import com.lz.Exception.MyException;
import com.lz.Exception.NoAthleteException;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lz
 * 
 * 通过全局异常处理的方式统一处理异常。
 */
@RestControllerAdvice
@RestController
@Slf4j
public class GlobalControllerAdvice {
    private static final String BAD_REQUEST_MSG = "客户端请求参数错误";
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);


    // <1> 处理 form data方式调用接口校验失败抛出的异常

    @ExceptionHandler(BindException.class)

    public Result<String> bindExceptionHandler(BindException e) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<String> collect = fieldErrors.stream()

                .map(DefaultMessageSourceResolvable::getDefaultMessage)

                .collect(Collectors.toList());

        return Result.error(collect.toString());

    }

    // <2> 处理 json 请求体调用接口校验失败抛出的异常

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public Result<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<String> collect = fieldErrors.stream()

                .map(DefaultMessageSourceResolvable::getDefaultMessage)

                .collect(Collectors.toList());

        return Result.error(collect.toString());

    }

    // <3> 处理单个参数校验失败抛出的异常

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> constraintViolationExceptionHandler(ConstraintViolationException e) {

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();

        List<String> collect = constraintViolations.stream()

                .map(ConstraintViolation::getMessage)

                .collect(Collectors.toList());

        return Result.error(collect.toString());

    }
    
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e){
        System.out.println("SQLIntegrityConstraintViolationException = "+e);
                

        return Result.error("SQLIntegrityConstraintViolationException");
    }

    /**
     * 异常处理程序
     * 处理SQL异常
     *
     * @param e e
     *
     * @return {@code Result}
     */
    @ExceptionHandler(value = SQLException.class)
    public Result<String> exceptionHandler(SQLException e)  {
       
        logger.error("发生SQL异常！原因是:",e);
        return Result.error(e.getMessage());
    }
    
    @ExceptionHandler(value = NullPointerException.class)
    public Result<String> exceptionHandler(NullPointerException e){
        logger.error("发生系统异常！原因是:",e);
        return Result.error(e.getMessage());
    }
    
    @ExceptionHandler(value = MyException.class)
    public Result<String> MyException(MyException e){
        logger.error("发生系统异常！原因是:",e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = NoAthleteException.class)
    public Result<Athlete> NoAthleteException(NoAthleteException e){
        logger.error("发生系统异常！原因是:",e);
        Athlete athlete = new Athlete();
        athlete.setAthleteState("未申请");
        return Result.error(athlete);
    }
    
}