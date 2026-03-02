package com.lz.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Mail Utils
 */
@Slf4j
@Component
public class MailUtils {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * Send Simple Mail
     */
    public void sendMail(String to, String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(title);
            message.setText(content);
            javaMailSender.send(message);
            log.info("邮件发送成功: {} -> {}", to, title);
        } catch (Exception e) {
            log.error("邮件发送失败: ", e);
            throw new RuntimeException("邮件发送失败");
        }
    }

    /**
     * Generate 6-digit verification code
     */
    public static String generateCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
