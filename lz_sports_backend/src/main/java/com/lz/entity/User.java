package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private String userType;

    @TableField("Status")
    private String status;

    @TableField("Email")
    private String email;

    @TableField("registerTime")
    private LocalDateTime registerTime;
}
