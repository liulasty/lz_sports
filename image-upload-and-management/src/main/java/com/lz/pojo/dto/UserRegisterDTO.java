package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/09/14:07
 * @Description:
 */

import com.lz.Annotation.HaveNoBlank;
import com.lz.Annotation.emailVerification;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lz
 */
@Data
public class UserRegisterDTO {
    @emailVerification
    private String username;
    @HaveNoBlank
    @NotBlank(message = "password 不能为空")
    private String password;
    @emailVerification
    private String email;
}