package com.lz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册数据传输对象
 * 用于接收前端传递的用户注册信息
 * 
 * @author Lombok Data Annotation
 * @version 1.0
 */
@Data
public class UserRegisterDTO {
    /**
     * 用户名
     * 用于用户登录和身份标识，不能为空
     */
    @NotBlank(message = "Username cannot be empty")
    private String username;

    /**
     * 密码
     * 用于用户身份验证，不能为空
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "密码需为8-20位且包含字母和数字"
    )
    private String password;

    /**
     * 电子邮箱地址
     * 用于接收验证码和用户联系，必须满足邮箱格式且不能为空
     */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
