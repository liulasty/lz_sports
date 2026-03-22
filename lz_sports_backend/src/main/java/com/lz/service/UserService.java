package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.dto.EventListDTO;
import com.lz.dto.UserLoginDTO;
import com.lz.entity.User;
import com.lz.common.result.PageResult;
import com.lz.vo.chart.UserData;
import com.lz.vo.chart.UserType;

import java.util.List;

/**
 * User Service Interface
 */
import com.lz.dto.UserRegisterDTO;

import com.lz.dto.UserUpdateDTO;
import com.lz.vo.UserDetailVO;

public interface UserService extends IService<User> {
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * Send verification code
     */
    void sendCode(String email, String scene);

    String verifyCode(String email, String code, String scene);

    /**
     * Approve or reject user
     */
    void auditUser(Long userId, Integer status, String reason);

    User login(UserLoginDTO userLoginDTO);

    UserDetailVO getUserDetail();

    void updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 重置密码
     */
    void resetPassword(com.lz.dto.ResetPasswordDTO resetPasswordDTO);

    PageResult list(EventListDTO listDto);

    void deleteUser(String id);

    void examinePlayer(String id);

    UserData getUserNumsByMonth(String month);

    List<UserType> getUserTypes();

    int[] getNums();

    User selectUserInfo();

    long getUnreadCount(Long userId);

    void saveLoginToken(Long userId, String token);
}
