package com.lz.Annotation.Validator;



import com.lz.Annotation.emailVerification;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 电子邮件验证器
 *
 * @author lz
 * @date 2023/11/08
 */
@Slf4j
public class emailValidator implements ConstraintValidator<emailVerification, String> {


    /**
     * 电子邮件模式
     * 将正则表达式作为静态常量进行初始化，提高可读性和可维护性
     */
    private static final String EMAIL_PATTERN = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,7}$";
    private static final String USERNAME_PATTERN = "^(\\w{1,9})$";
    /**
     * 将Pattern对象声明为静态成员变量，避免每次调用isValid时重复编译正则表达式
     */
    private static final Pattern EMAIL_PATTERN_PATTERN = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern USERNAME_PATTERN_PATTERN = Pattern.compile(USERNAME_PATTERN);

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        // 对null值的处理
        if (s == null) {
            log.debug("输入值为null，验证失败");
            return false;
        }

        // 使用静态成员变量EMAIL_PATTERN_PATTERN进行匹配
        Matcher emailMatcher = EMAIL_PATTERN_PATTERN.matcher(s);
        if (!emailMatcher.matches()) {
            log.debug("不是邮箱格式: {}", s);
            // 使用静态成员变量EMAIL_PATTERN_PATTERN进行匹配
            Matcher usernameMatcher = USERNAME_PATTERN_PATTERN.matcher(s);
            if (!usernameMatcher.matches()) {
                log.debug("用户名格式: {}", s);

                return false;
            }
        }

        // 如果到达这里，表示电子邮件格式校验通过，无需再进行用户名格式的检查
        return true;
    }
}