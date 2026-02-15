package com.lz.Annotation;

import com.lz.Annotation.Validator.HaveNoBlankValidator;
import com.lz.Annotation.Validator.emailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 *
 * @author lz
 * 
 * 
 * @description:登录账户校验
 * @date 2024/04/03
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {emailValidator.class})// 标明由哪个类执行校验逻辑
public @interface emailVerification {
    // 校验出错时默认返回的消息
    String message() default "邮箱或用户名格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 同一个元素上指定多个该注解时使用
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        NotBlank[] value() default {};
    }
}