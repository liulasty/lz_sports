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
@TableName("sys_user")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long userId;

    @TableField("username")
    private String userName;

    @TableField("password")
    private String password;

    @TableField("role")
    private UserRole userType;

    @TableField("status")
    private UserStatus status;

    @TableField("email")
    private String email;

    @TableField("create_time")
    private LocalDateTime registerTime;

    @TableField("school_id")
    private Long schoolId;

    @TableField("name")
    private String name;

    @TableField("gender")
    private String gender;

    @TableField("student_id")
    private String studentId;

    @TableField("grade_id")
    private Long gradeId;
}
