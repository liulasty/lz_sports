package com.lz.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * User Update DTO
 */
@Data
public class UserUpdateDTO {
    private Long userId;
    private String userName;
    private String email;
    private String oldPassword;
    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "新密码需为8-20位且包含字母和数字"
    )
    private String newPassword;
}
