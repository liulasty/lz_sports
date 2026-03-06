# 用户审核通知功能测试说明

## 测试类位置
`src/test/java/com/lz/UserAuditNotificationTest.java`

## 测试内容

该测试类包含以下测试场景：

### 1. `testAuditUserApproval()` - 审核通过测试
- **目的**: 验证用户审核通过时的完整流程
- **测试点**:
  - 用户状态从 PENDING 更新为 ACTIVE
  - 发送审核通过通知邮件
  - 数据库记录正确更新

### 2. `testAuditUserRejection()` - 审核拒绝测试
- **目的**: 验证用户审核拒绝时的完整流程
- **测试点**:
  - 用户状态从 PENDING 更新为 REJECTED
  - 发送审核拒绝通知邮件（包含拒绝原因）
  - 数据库记录正确更新

### 3. `testSendAuditResultMailApproval()` - 直接邮件发送测试（通过）
- **目的**: 单独测试 MailUtils 的审核通过邮件发送功能
- **注意**: 需要将测试邮箱地址修改为实际可用的邮箱

### 4. `testSendAuditResultMailRejection()` - 直接邮件发送测试（拒绝）
- **目的**: 单独测试 MailUtils 的审核拒绝邮件发送功能
- **注意**: 需要将测试邮箱地址修改为实际可用的邮箱

### 5. `testAuditNonExistentUser()` - 异常处理测试
- **目的**: 验证对不存在的用户进行审核时的异常处理
- **测试点**:
  - 正确抛出 BusinessException
  - 异常信息准确

## 运行测试前的准备

### 1. 配置测试数据库
确保 MySQL 数据库中存在 `lz_sports_test` 数据库，或者修改 `src/test/resources/application-test.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/你的测试数据库名
    username: 你的用户名
    password: 你的密码
```

### 2. 配置 Redis
确保 Redis 服务正在运行，或者修改 `src/test/resources/application-test.yml` 中的 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的 Redis 密码（如果有）
```

### 3. 配置邮件服务
确保邮件服务配置正确，或者修改 `src/test/resources/application-test.yml` 中的邮件配置：

```yaml
spring:
  mail:
    host: smtp.qq.com
    username: 你的 QQ 邮箱
    password: 你的 SMTP 授权码
```

### 4. 修改测试邮箱
在测试类中，将以下测试方法中的邮箱地址修改为你自己的邮箱：

```java
// testSendAuditResultMailApproval() 和 testSendAuditResultMailRejection()
String testSubjectEmail = "your-test-email@example.com"; // 修改为你的真实邮箱
```

## 运行测试

### 使用 Maven 运行所有测试
```bash
cd lz_sports_backend
mvn test
```

### 使用 Maven 运行单个测试类
```bash
cd lz_sports_backend
mvn test -Dtest=UserAuditNotificationTest
```

### 使用 IDE 运行
在 IDEA 或其他 IDE 中，右键点击测试类或测试方法，选择 "Run Test" 或 "Debug Test"。

## 测试输出说明

测试运行时会在控制台输出详细信息：

```
========== 开始测试审核通过场景 ==========
✓ 用户状态已更新为：ACTIVE
✓ 审核通过邮件已发送至：test@qq.com
========== 审核通过场景测试完成 ==========
```

## 注意事项

1. **异步邮件发送**: 由于邮件发送是异步的，测试中使用了 `Thread.sleep(2000)` 来等待邮件发送完成。在实际环境中，可能需要根据网络情况调整等待时间。

2. **测试数据清理**: 每个测试用例执行前都会创建新的测试用户，测试完成后建议手动清理测试数据。

3. **邮件接收**: 如果配置的邮箱不是真实的，邮件发送会失败但不影响测试结果（因为邮件发送是异步的，失败会被捕获）。

4. **数据库隔离**: 建议使用独立的测试数据库，避免影响开发或生产数据。

## 常见问题

### Q: 测试失败，提示无法连接数据库
A: 检查测试数据库配置，确保数据库服务正在运行，并且数据库已创建。

### Q: 邮件没有收到
A: 
- 检查邮件配置是否正确
- 确认 SMTP 授权码是否有效
- 查看垃圾邮件文件夹
- 检查日志输出中的错误信息

### Q: Redis 连接失败
A: 确保 Redis 服务正在运行，并且端口、密码配置正确。

## 扩展测试

可以根据需要添加更多测试用例，例如：
- 批量审核测试
- 并发审核测试
- 邮件发送失败的场景测试
- 不同用户类型的审核测试
