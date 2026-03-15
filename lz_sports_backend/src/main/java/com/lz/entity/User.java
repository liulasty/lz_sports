package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 * 继承基类，仅保留业务特有字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // 继承基类需添加此注解
@TableName("sys_user")
public class User extends SchoolRelatedEntity<Long> {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 学号
     */
    @TableField("student_id")
    private String studentId;

    /**
     * 年级ID
     */
    @TableField("grade_id")
    private Long gradeId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色
     */
    @TableField("user_type")
    private UserRole userType;

    /**
     * 用户状态
     */
    private UserStatus status;
}