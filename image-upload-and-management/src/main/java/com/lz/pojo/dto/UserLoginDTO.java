package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/05/16:49
 * @Description:
 */

import com.lz.Annotation.HaveNoBlank;
import com.lz.Annotation.emailVerification;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lz
 */
@Data
public class UserLoginDTO {
    @emailVerification
    private String username;
    @HaveNoBlank
    @NotBlank(message = "password 不能为空")
    private String password;
}