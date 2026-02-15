package com.lz.dto;

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
    private String newPassword;
}
