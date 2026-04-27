package com.lz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Login VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {
    private Long id;
    private String userName;
    private String role;
    private String type;
    private String userType;
    private Boolean isFirstLogin;
    private Long unreadCount;
    private String avatarSrc;
    private String token;
}
