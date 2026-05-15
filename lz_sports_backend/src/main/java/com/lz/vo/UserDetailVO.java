package com.lz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Detail VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO {
    private Long userId;
    private String userName;
    private String role;
    private String type;
    private String userType;
    private String email;
    private String status;
    private String avatarSrc;
    private LocalDateTime registerTime;
    private String name;
    private String gender;
    private String contact;
    private Long deptId;
}
