package com.lz.service;

public interface EmailService {
    /**
     * 发送验证码
     * @param email 邮箱地址
     * @param scene 业务场景 (register/login/reset)
     */
    void sendVerificationCode(String email, String scene);

    /**
     * 校验验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param scene 业务场景
     * @return 是否校验成功
     */
    boolean verifyCode(String email, String code, String scene);
}
