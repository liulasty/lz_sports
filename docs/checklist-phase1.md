# Phase 1 自检清单 · 基础骨架

> 使用方式：每项完成后将 [ ] 改为 [x]
> 全部为 [x] 才允许进入 Phase 2

---

## 🗄️ 数据库

- [x] init.sql 包含全部 10 张表的建表语句
- [x] 每张表都有 created_at / updated_at 字段
- [x] sys_user.email 有唯一索引
- [x] notification 表有 idx_user_read (user_id, is_read) 复合索引
- [x] data.sql 包含全部内置项目初始数据（至少12条）
- [x] school_config 表有 is_initialized 字段

---

## ⚙️ 项目配置

- [x] application.yml 数据库连接使用占位符（不硬编码账号密码）
- [x] application-dev.yml 和 application-prod.yml 分别存在
- [x] Redis 连接已配置
- [x] 跨域配置已开启（允许前端开发端口）
- [x] 异步线程池已配置（@EnableAsync）
- [x] 定时任务已开启（@EnableScheduling）
- [x] 文件上传目录已配置，静态资源可通过 URL 访问

---

## 🔐 JWT 与 Security

- [x] JwtUtil 有 generateToken / parseToken / isExpired 三个方法
- [x] token 有效期已设置（7天），不是永不过期
- [x] JwtAuthFilter 正确从 Header 取 token 并注入 SecurityContext
- [x] 以下路径已放行（无需登录）：
  - [x] POST /api/system/init-status
  - [x] POST /api/system/init
  - [x] POST /api/auth/send-code
  - [x] POST /api/auth/verify-code
  - [x] POST /api/auth/register
  - [x] POST /api/auth/login
  - [x] POST /api/auth/reset-password
  - [x] GET /api/public/**
- [x] 其余路径未登录访问返回 401（不是 403，不是重定向）
- [x] @RequireRole 注解 + 切面已实现，角色不符返回 403
- [x] @RequireEventAdmin 注解 + 切面已实现，越权返回 403

---

## 📧 邮件与验证码

- [x] EmailService 有 sendVerificationCode 方法
- [x] 发送方法有 @Async 注解（异步，不阻塞主流程）
- [x] 验证码存入 Redis，key 格式：code:{scene}:{email}
- [x] 验证码有效期 10 分钟
- [x] 同一邮箱 60 秒内重复发送返回 409，并告知剩余冷却秒数
- [x] verifyToken 存入 Redis，有效期 30 分钟，使用一次即失效

---

## 🚀 系统初始化

- [x] GET /api/system/init-status 正确返回 initialized 状态
- [x] POST /api/system/init 在一个 @Transactional 中完成：
  - [x] 写入 school_config 记录
  - [x] 创建超管账号（密码 BCrypt 加密）
  - [x] 写入内置项目库数据
- [x] 已初始化后再次调用 init 接口返回 409
- [x] 初始化完成后 is_initialized 置为 1

---

## 👤 用户注册与登录

- [x] 注册接口校验 verifyToken 有效性
- [x] 同一邮箱重复注册返回 409
- [x] 注册成功后账号直接 ACTIVE，无需审核
- [x] 密码符合强度要求（8-20位，含字母和数字）
- [x] 登录接口密码错误统一提示「邮箱或密码错误」（不区分是邮箱不存在还是密码错）
- [x] 账号 DISABLED 时登录返回具体提示
- [x] 登录成功返回 token、role、isFirstLogin、unreadCount
- [x] 超管首次登录 isFirstLogin = true，前端跳转强制改密码页
- [x] 改密码成功后 is_first_login 置为 0，当前 token 失效

---

## 🛡️ 权限边界验证

- [x] @RequireRole 注解 + 切面已实现，角色不符返回 403
- [x] @RequireEventAdmin 注解 + 切面已实现，越权返回 403
- [ ] 用 USER 角色 token 访问 /api/admin/** 返回 403
- [ ] 用 EVENT_ADMIN token 访问 /api/admin/** 返回 403
- [ ] 不带 token 访问需要登录的接口返回 401
- [ ] token 过期后访问返回 401

---

## 🎨 前端基础

- [ ] CSS 变量文件存在，包含颜色、圆角、间距变量
- [ ] 6套主题色文件全部存在
- [ ] Axios 封装完成：自动携带 token，401 时跳登录页，统一错误提示
- [ ] 路由守卫：未登录访问需要登录的页面跳转到登录页
- [ ] 路由守卫：已初始化时访问 /setup 跳转到首页
- [ ] 路由守卫：未初始化时访问任意页面跳转到 /setup
- [ ] 初始化向导 4 步流程完整，步骤条显示正确
- [ ] 超管首次登录强制改密码，未改密码前不可访问其他页面
- [ ] 登录页展示学校 Logo 和校名（从 public/school-config 接口获取）

---

## 代码质量

- [ ] 无遗留 System.out.println
- [ ] 无遗留 console.log
- [ ] 无空 catch 块
- [ ] 无硬编码密码或密钥
- [x] 所有 Controller 入参有 @Valid 注解
