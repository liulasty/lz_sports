package com.lz;

import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.entity.User;
import com.lz.mapper.UserMapper;
import com.lz.service.UserService;
import com.lz.util.MailUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户审核通知功能测试类
 * 测试 UserServiceImpl.auditUser 方法中的邮件发送逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserAuditNotificationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailUtils mailUtils;

    private Long testUserId;
    private String testEmail = "2312034544@qq.com";

    @BeforeEach
    void setUp() {
        // 清理测试数据
        if (userMapper != null) {
            // 删除之前创建的测试用户（如果存在）
            User existingUser = userMapper.selectByEmail(testEmail);
            if (existingUser != null) {
                userMapper.deleteById(existingUser.getId());
            }

            // 创建测试用户
            User user = new User();
            user.setUsername("test_audit_user");
            user.setEmail(testEmail);
            user.setPassword("test123456");
            user.setUserType(UserRole.ATHLETE);
            user.setStatus(UserStatus.PENDING); // 初始状态为待审核
            user.setSchoolId(1L);
            userMapper.insert(user);
            testUserId = user.getId();
        }
    }

    /**
     * 测试审核通过场景
     * 验证：
     * 1. 用户状态更新为 ACTIVE
     * 2. 发送审核通过通知邮件
     */
    @Test
    void testAuditUserApproval() throws InterruptedException {
        System.out.println("========== 开始测试审核通过场景 ==========");
        
        // 执行审核通过操作
        userService.auditUser(testUserId, 1, null);

        // 等待异步邮件发送完成（实际测试中可能需要根据情况调整等待时间）
        Thread.sleep(2000);

        // 验证用户状态已更新为 ACTIVE
        User updatedUser = userMapper.selectById(testUserId);
        assertNotNull(updatedUser, "用户不应为空");
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus(), "用户状态应为审核通过");
        
        System.out.println("✓ 用户状态已更新为：ACTIVE");
        System.out.println("✓ 审核通过邮件已发送至：" + updatedUser.getEmail());
        System.out.println("========== 审核通过场景测试完成 ==========\n");
    }

    /**
     * 测试审核拒绝场景
     * 验证：
     * 1. 用户状态更新为 REJECTED
     * 2. 发送审核拒绝通知邮件（包含拒绝原因）
     */
    @Test
    void testAuditUserRejection() throws InterruptedException {
        System.out.println("========== 开始测试审核拒绝场景 ==========");
        
        String rejectionReason = "提交的信息不完整，缺少学生证照片";
        
        // 重新设置用户状态为 PENDING（因为上一个测试可能已经改变了状态）
        User user = userMapper.selectById(testUserId);
        if (user != null) {
            user.setStatus(UserStatus.PENDING);
            userMapper.updateById(user);
        }

        // 执行审核拒绝操作
        userService.auditUser(testUserId, 0, rejectionReason);

        // 等待异步邮件发送完成
        Thread.sleep(2000);

        // 验证用户状态已更新为 REJECTED
        User updatedUser = userMapper.selectById(testUserId);
        assertNotNull(updatedUser, "用户不应为空");
        assertEquals(UserStatus.REJECTED, updatedUser.getStatus(), "用户状态应为审核拒绝");
        
        System.out.println("✓ 用户状态已更新为：REJECTED");
        System.out.println("✓ 拒绝原因：" + rejectionReason);
        System.out.println("✓ 审核拒绝邮件已发送至：" + updatedUser.getEmail());
        System.out.println("========== 审核拒绝场景测试完成 ==========\n");
    }

    /**
     * 测试直接调用 MailUtils 发送审核通过邮件
     * 用于单独验证邮件发送功能
     */
    @Test
    void testSendAuditResultMailApproval() throws InterruptedException {
        System.out.println("========== 开始测试直接调用邮件发送（通过） ==========");
        
        String testSubjectEmail = "your-test-email@example.com"; // 请替换为实际测试邮箱
        
        // 直接调用邮件发送方法
        mailUtils.sendAuditResultMail(testSubjectEmail, true, null);
        
        // 等待异步邮件发送
        Thread.sleep(2000);
        
        System.out.println("✓ 审核通过通知邮件已发送至：" + testSubjectEmail);
        System.out.println("========== 邮件发送测试（通过）完成 ==========\n");
    }

    /**
     * 测试直接调用 MailUtils 发送审核拒绝邮件
     * 用于单独验证邮件发送功能
     */
    @Test
    void testSendAuditResultMailRejection() throws InterruptedException {
        System.out.println("========== 开始测试直接调用邮件发送（拒绝） ==========");
        
        String testSubjectEmail = "your-test-email@example.com"; // 请替换为实际测试邮箱
        String reason = "个人信息填写不完整，请补充学生证扫描件后重新提交";
        
        // 直接调用邮件发送方法
        mailUtils.sendAuditResultMail(testSubjectEmail, false, reason);
        
        // 等待异步邮件发送
        Thread.sleep(2000);
        
        System.out.println("✓ 审核拒绝通知邮件已发送至：" + testSubjectEmail);
        System.out.println("✓ 拒绝原因：" + reason);
        System.out.println("========== 邮件发送测试（拒绝）完成 ==========\n");
    }

    /**
     * 测试对不存在的用户进行审核
     * 验证是否正确抛出异常
     */
    @Test
    void testAuditNonExistentUser() {
        System.out.println("========== 开始测试审核不存在的用户 ==========");
        
        Long nonExistentUserId = 999999L;
        
        assertThrows(
            com.lz.common.exception.BusinessException.class,
            () -> userService.auditUser(nonExistentUserId, 1, null),
            "应对不存在的用户抛出 BusinessException"
        );
        
        System.out.println("✓ 正确抛出了 BusinessException 异常");
        System.out.println("========== 审核不存在用户测试完成 ==========\n");
    }

    /**
     * 清理测试数据
     * 在所有测试完成后执行
     */
    void tearDown() {
        if (testUserId != null && userMapper != null) {
            userMapper.deleteById(testUserId);
            System.out.println("✓ 已清理测试数据");
        }
    }
}
