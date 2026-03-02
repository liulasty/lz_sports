package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    @TableId(value = "UserID", type = IdType.AUTO)
    private Long userId;

    @TableField("Username")
    private String userName;

    @TableField("Password")
    private String password;

    @TableField("UserType")
    private UserRole userType;

    @TableField("Status")
    private UserStatus status;

    @TableField("Email")
    private String email;

    @TableField("registerTime")
    private LocalDateTime registerTime;

    @TableField("school_id")
    private Long schoolId;
}
