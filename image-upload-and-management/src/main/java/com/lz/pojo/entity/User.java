package com.lz.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sun.tracing.dtrace.ArgsAttributes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author lz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @TableId(value = "UserID",type = IdType.AUTO)
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
    private Date registerTime;
}