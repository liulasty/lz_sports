# AGENT.md · lz_sports 开发工作规范

> 本文件是 AI 在整个项目开发期间必须遵守的工作手册。
> 每次对话开始前必须先读取本文件。

---

## 一、项目概述

校园运动会报名与成绩管理系统，Spring Boot 21 + Vue 3，
前后端分开部署，以 Jar 包形式交付给单个学校独立使用。

---

## 二、技术栈速查

### 后端
| 技术           | 版本     |
| ------------ | ------ |
| JDK          | 21     |
| Spring Boot  | 3.2.x  |
| MyBatis-Plus | 3.5.x  |
| MySQL        | 8.x    |
| Redis        | 7.x    |
| JJWT         | 0.12.x |
| EasyExcel    | 3.3.x  |
| Hutool       | 5.8.x  |

### 前端
| 技术           | 版本    |
| ------------ | ----- |
| Vue          | 3.4.x |
| Vue Router   | 4.x   |
| Pinia        | 2.x   |
| Element Plus | 2.6.x |
| Vite         | 5.x   |
| Node         | 18+   |

---

## 三、项目结构速查

### 后端
```
src/main/java/com/lzsports/
├── common/          Result、PageResult、枚举、异常
├── config/          Security、Redis、Cors、Async、Mvc
├── controller/      所有Controller
├── service/         接口 + impl实现
├── mapper/          MyBatis-Plus Mapper
├── entity/          数据库实体
├── dto/             request/ + response/
├── utils/           Jwt、Password、File工具
├── security/        JwtFilter、UserContext
├── annotation/      RequireRole、RequireEventAdmin
├── aspect/          RoleCheck、EventAdminCheck切面
└── scheduler/       EventStatus定时任务
```

### 前端
```
src/
├── api/             所有接口请求模块
├── components/      公共组件
├── router/          路由配置
├── stores/          Pinia状态
├── styles/          CSS变量 + 6套主题
├── utils/           request、auth工具
└── views/
    ├── setup/       初始化向导
    ├── auth/        登录注册
    ├── home/        首页
    ├── event/       赛事相关（用户侧）
    ├── my/          个人中心
    ├── public/      公开成绩
    └── admin/       管理后台
```

---

## 四、数据库速查

共10张表：

| 表名                   | 核心字段                                     | 说明          |
| -------------------- | ---------------------------------------- | ----------- |
| school_config        | id, school_name, logo_url, theme_code    | 全库唯一一条      |
| sys_user             | id, email, password, role, status, unread_count | 所有用户        |
| project_library      | id, name, category, type                 | STANDARD不可删 |
| notification         | id, user_id, type, is_read               | 站内信         |
| event                | id, name, status, reg_start_time, max_items_per_athlete | 赛事主表        |
| event_item           | id, event_id, project_id, current_count, max_participants | 赛事项目        |
| event_admin_mapping  | event_id, user_id                        | 管理员绑定       |
| athlete_registration | id, user_id, event_id, status            | 运动员申请       |
| item_registration    | id, user_id, event_id, event_item_id, status | 项目报名        |
| result               | id, event_item_id, user_id, score, rank, is_published | 成绩          |

---

## 五、接口规范

**Base URL**：`/api/v1`

**认证方式**：`Authorization: Bearer {token}`

**统一响应**：
```json
{ "code": 200, "message": "success", "data": {} }
```

**错误码**：
| code | 含义    |
| ---- | ----- |
| 400  | 参数错误  |
| 401  | 未登录   |
| 403  | 无权限   |
| 404  | 不存在   |
| 409  | 业务冲突  |
| 500  | 服务器错误 |

---

## 六、执行纪律（每次任务必须遵守）

### 开始前
- [ ] 读取本 AGENT.md 文件
- [ ] 读取当前 Phase 对应的 docs/checklist-phaseX.md
- [ ] 列出本次任务的完整子任务清单
- [ ] 等待开发者确认后再动手

### 执行中
- 每完成一个子任务，立即输出：
  `✅ 已完成：[文件路径] - [具体做了什么]`
- 遇到任何不确定的地方，立即停下来问，不自行假设
- 不允许因为「差不多」「类似」而跳过任何细节
- 不允许在没完成当前子任务的情况下开始下一个

### 完成后
- 对照 checklist 逐条打勾
- 输出本次修改的所有文件清单
- 明确说明是否有未完成项

---

## 七、代码规范

### 后端规范
```
✅ 所有 Controller 入参必须加 @Valid 注解
✅ 所有多表写操作必须加 @Transactional
✅ 所有邮件和通知发送必须加 @Async（异步）
✅ 所有密码必须 BCrypt 加密，禁止明文存储
✅ 不允许在 Controller 层写业务逻辑
✅ 不允许硬编码魔法值，使用枚举或常量
✅ 并发扣减名额必须使用原子 UPDATE，禁止先查后改
✅ STANDARD 类型项目的删除拦截必须在 Service 层
✅ 超管账号的保护逻辑必须在 Service 层
```

### 前端规范
```
✅ 所有颜色值必须使用 CSS 变量，禁止硬编码
✅ 所有 API 请求必须通过 utils/request.js 统一封装
✅ token 统一由 utils/auth.js 存取，不直接操作 localStorage
✅ 路由权限守卫在 router/index.js 统一处理
✅ 不允许在组件内直接写 axios 请求
```

### 禁止事项
```
❌ 禁止遗留 System.out.println 或 console.log 调试代码
❌ 禁止遗留 TODO / FIXME 注释（完成后清理）
❌ 禁止未处理的 catch 块（空 catch 或只打印堆栈）
❌ 禁止接口返回裸数据（必须包裹在 Result<T> 中）
❌ 禁止前端直接展示后端原始错误信息给用户
```

---

## 八、关键业务规则速查
```
报名名额控制：
  UPDATE event_item SET current_count = current_count + 1
  WHERE id = ? AND current_count < max_participants
  影响行数为0 → 名额已满，返回409

成绩发布：
  is_published = 1 后不可修改
  发布后异步向所有该赛事运动员发站内通知

运动员身份：
  赛事级别，不是全局
  每个赛事单独申请，单独审核

项目删除保护：
  category = STANDARD → 禁止删除（403）
  有报名记录 → 禁止删除（409）

赛事状态流转（定时任务每分钟检查）：
  OPEN + 当前时间 > reg_end_time → CLOSED
  CLOSED + 当前时间 > event_start_time → ONGOING
  ONGOING + 当前时间 > event_end_time → FINISHED

未读消息计数：
  发送通知时 unread_count + 1（UPDATE sys_user）
  标记已读时 unread_count - 1
  全部已读时 unread_count = 0
```

---

## 九、各 Phase 自检清单索引

| Phase   | 目标    | 自检文件                     |
| ------- | ----- | ------------------------ |
| Phase 1 | 基础骨架  | docs/checklist-phase1.md |
| Phase 2 | 赛事核心  | docs/checklist-phase2.md |
| Phase 3 | 成绩与通知 | docs/checklist-phase3.md |
| Phase 4 | 管理与收尾 | docs/checklist-phase4.md |

---

## 十、完成标准

> 每个 Phase 必须通过对应自检清单的全部条目，
> 才允许提交并进入下一 Phase。
> 有任何 ❌ 未完成项，必须修复后重新过清单。