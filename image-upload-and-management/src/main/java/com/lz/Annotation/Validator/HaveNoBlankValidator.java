package com.lz.Annotation.Validator;

import com.lz.Annotation.HaveNoBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * 没有空白验证器
 *
 * @author lz
 * @date 2023/11/06
 */
public class HaveNoBlankValidator implements ConstraintValidator<HaveNoBlank, String> {

    // 使用正则表达式匹配所有空白字符
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 对null值的处理策略，此处保持为true，表示null视为有效输入
        if (value == null) {
            return true;
        }

        // 使用正则表达式匹配字符串中的任何空白字符
        Matcher matcher = WHITESPACE_PATTERN.matcher(value);
        if (matcher.find()) {
            // 如果找到空白字符，则校验失败
            // 可以通过context.disableDefaultConstraintViolation()和context.buildConstraintViolationWithTemplate()向用户提供更详细的错误信息
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("输入字符串中包含不允许的空白字符").addConstraintViolation();
            return false;
        }

        // 校验成功
        return true;
    }

}