package com.lz.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
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
     * 发送HTML格式邮件 (异步)
     * @param to 收件人邮箱
     * @param title 邮件主题
     * @param htmlContent 邮件HTML内容
     */
    @Async // 启用异步执行，不阻塞主线程
    public void sendHtmlMail(String to, String title, String htmlContent) {
        try {
            log.info("正在发送HTML邮件至: {}, 主题: {}", to, title);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(htmlContent, true); // true表示启用HTML解析

            javaMailSender.send(mimeMessage);
            log.info("HTML邮件发送成功: {} -> {}", to, title);
        } catch (Exception e) {
            log.error("HTML邮件发送失败: ", e);
        }
    }

    /**
     * 发送带样式的验证码邮件 (异步)
     * @param to 收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCodeMail(String to, String code) {
        String title = "【您的应用名】您的验证码";
        String htmlContent = buildVerificationCodeHtml(code);
        log.info("正在发送验证码邮件至: {}, 验证码: {}", to, code);
        sendHtmlMail(to, title, htmlContent);
    }

    /**
     * 发送审核结果通知邮件 (异步)
     * @param to 收件人邮箱
     * @param status 审核状态 (true: 通过, false: 拒绝)
     * @param reason 拒绝原因 (如果status为false)
     */
    public void sendAuditResultMail(String to, boolean status, String reason) {
        String title;
        String message;
        String color; // 用于区分通过(绿色)和拒绝(红色)的颜色

        if (status) {
            title = "【您的应用名】账号审核通过通知";
            message = "恭喜您！您的账号已通过审核，现在可以登录系统，开始使用各项功能了。";
            color = "#27ae60"; // 绿色
        } else {
            title = "【您的应用名】账号审核未通过通知";
            message = "很遗憾，您的账号审核未通过。具体原因如下：<br/><br/>" + reason;
            color = "#e74c3c"; // 红色
        }

        String htmlContent = buildAuditResultHtml(message, color);
        sendHtmlMail(to, title, htmlContent);
    }

    /**
     * 构建验证码邮件的HTML内容
     */
    private String buildVerificationCodeHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>验证码</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color: #f5f7fa;">
                        <tr>
                            <td style="padding: 20px 0;">
                                <!-- Main Content -->
                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" align="center" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 30px 30px 20px 30px; text-align: center; background-color: #4A90E2; color: #ffffff;">
                                            <h1 style="margin: 0; font-size: 24px; font-weight: normal;">您的验证码</h1>
                                        </td>
                                    </tr>
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="margin: 0 0 20px 0; font-size: 16px; color: #555555; line-height: 1.6;">您好！</p>
                                            <p style="margin: 0 0 20px 0; font-size: 16px; color: #555555; line-height: 1.6;">您正在进行账户验证，本次操作的验证码为：</p>
                                            
                                            <div style="text-align: center; margin: 30px 0;">
                                                <span style="display: inline-block; padding: 15px 0; min-width: 200px; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #4A90E2; background-color: #f0f8ff; border: 2px dashed #4A90E2; border-radius: 8px;">%s</span>
                                            </div>

                                            <p style="margin: 20px 0 0 0; font-size: 14px; color: #999999; line-height: 1.5;">
                                                此验证码 <strong style="color: #e74c3c;">5分钟</strong> 内有效。请勿将验证码告知他人。
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 20px 30px; text-align: center; background-color: #f9fafb; border-top: 1px solid #eee;">
                                            <p style="margin: 0; font-size: 12px; color: #999999; line-height: 1.5;">
                                                &copy; %d %s. 版权所有.<br/>
                                                这是一封自动发送的邮件，请勿直接回复。
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                code, // 验证码
                new Date().getYear() + 1900, // 当前年份
                "YourAppName" // 请替换为你的应用名称
        );
    }

    /**
     * 构建审核结果通知邮件的HTML内容
     * @param message 邮件主体消息
     * @param color 根据状态决定的主题颜色
     * @return HTML格式的邮件内容
     */
    private String buildAuditResultHtml(String message, String color) {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>账号审核结果</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color: #f5f7fa;">
                        <tr>
                            <td style="padding: 20px 0;">
                                <!-- Main Content -->
                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" align="center" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 30px 30px 20px 30px; text-align: center; background-color: %s; color: #ffffff;">
                                            <h1 style="margin: 0; font-size: 24px; font-weight: normal;">账号审核结果通知</h1>
                                        </td>
                                    </tr>
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="margin: 0 0 20px 0; font-size: 16px; color: #555555; line-height: 1.6;">尊敬的用户，您好！</p>
                                            <div style="padding: 20px; border-left: 4px solid %s; background-color: #f9f9f9; margin: 20px 0;">
                                                <p style="margin: 0; font-size: 16px; color: #555555; line-height: 1.6;">%s</p>
                                            </div>
                                            <p style="margin: 20px 0 0 0; font-size: 14px; color: #999999; line-height: 1.5;">
                                                如果您对审核结果有任何疑问，请联系我们的客服。
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 20px 30px; text-align: center; background-color: #f9fafb; border-top: 1px solid #eee;">
                                            <p style="margin: 0; font-size: 12px; color: #999999; line-height: 1.5;">
                                                &copy; %d %s. 版权所有.<br/>
                                                这是一封自动发送的邮件，请勿直接回复。
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                color, // 头部背景色
                color, // 左侧强调边框色
                message, // 邮件主体消息
                new Date().getYear() + 1900, // 当前年份
                "YourAppName" // 请替换为你的应用名称
        );
    }

    /**
     * Generate 6-digit verification code
     */
    public static String generateCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * Test method for sending a sample verification code email.
     * This method can be called to quickly verify the email functionality.
     * Remember to set a valid 'to' email address before testing.
     */
    public void test() {
        // TODO: 请将下面的邮箱地址修改为您自己的真实邮箱进行测试
        String testEmail = "your-test-email@example.com";
        String testCode = generateCode();

        log.info("=== 开始执行邮件发送测试 ===");
        log.info("目标邮箱: {}", testEmail);
        log.info("验证码: {}", testCode);

        sendVerificationCodeMail(testEmail, testCode);

        log.info("=== 测试指令已发出 (邮件已异步发送) ===");
    }

    /**
     * Test method for sending a sample audit result notification email.
     * This method can be called to quickly verify the audit notification functionality.
     * Remember to set a valid 'to' email address before testing.
     */
    public void testAuditNotification() {
        // TODO: 请将下面的邮箱地址修改为您自己的真实邮箱进行测试
        String testEmail = "your-test-email@example.com";

        log.info("=== 开始执行审核通知邮件发送测试 ===");
        log.info("目标邮箱: {}", testEmail);

        // 测试发送一个审核通过的通知
        sendAuditResultMail(testEmail, true, null);

        log.info("=== 审核通知测试指令已发出 (邮件已异步发送) ===");
    }
}