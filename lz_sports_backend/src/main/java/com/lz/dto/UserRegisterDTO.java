package com.lz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * User Register DTO
 */
@Data
public class UserRegisterDTO {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;
}
