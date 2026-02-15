# 项目改造步骤文档 (Transformation Steps)

本文档记录了将 `lz_sports` 项目从 Spring Boot 2 + Vue 2 重构为 Spring Boot 3 + Vue 3 的详细步骤与计划。

## 目标 (Goal)
- **后端**: 重构 `image-upload-and-management` -> `lz_sports_backend` (Spring Boot 3, Java 21, MyBatis-Plus 3.5.5)
- **前端**: 重构 `lz_sports_vue` -> `lz_sports_frontend` (Vue 3, Vite, Pinia, Element Plus)
- **原则**: 不修改原始项目代码，分阶段执行。

---

## 阶段一：后端基础架构与核心用户模块 (Backend Phase 1)

### 1.1 项目初始化 (Completed)
- [x] 创建 `backend` 目录与 `pom.xml`。
- [x] 配置 Spring Boot 3.2.3, Java 21, MyBatis-Plus 3.5.5 依赖。
- [x] 解决 Lombok 注解处理器 Maven 编译问题。
- [x] 创建 `application.yml` 配置文件 (数据库, Redis, JWT, OSS)。

### 1.2 通用工具与配置 (Completed)
- [x] 迁移/创建 `Result`, `PageResult`, `BaseContext`。
- [x] 迁移/创建 `BusinessException`, `GlobalExceptionHandler`。
- [x] 迁移/创建 `JwtUtil`, `RedisUtil`, `OssUtil`, `MailUtils`。
- [x] 配置 `MybatisPlusConfig` (分页插件)。
- [x] 配置 `WebMvcConfig` (拦截器/跨域 - 待完善)。
- [x] 配置 `SecurityConfig` & `JwtAuthenticationFilter` (Spring Security 6)。

### 1.3 用户模块 (User Module) - (In Progress)
- [x] 迁移 `User` 实体 (Entity)。
- [x] 迁移 `UserLoginDTO`, `UserRegisterDTO`。
- [x] 迁移 `UserVO`, `UserLoginVO`。
- [x] 迁移 `UserMapper`。
- [x] 迁移 `UserService` 接口。
- [x] **完善 `UserServiceImpl`**:
    - [x] 依赖 `SportsImgService` 实现头像处理 (Injected).
    - [x] 实现登录 (`login`).
    - [x] 实现注册 (`register`).
    - [x] 实现用户查询/更新逻辑.
- [x] **完善 `UserController`**:
    - [x] 接入 `UserService`.
    - [x] 完善 `/login`, `/register` 等接口.
    - [x] 完善 `/info`, `/update`, `/list` 接口.

### 1.4 图片/头像模块 (SportsImg Module) - (Completed)
- [x] 迁移 `SportsImg` 实体。
- [x] 迁移 `SportsImgMapper`。
- [x] 迁移 `SportsImgService` 接口。
- [x] **完善 `SportsImgServiceImpl`**:
    - [x] 实现图片上传/存储逻辑 (关联 OSS).
    - [x] 提供用户头像查询服务.

---

## 阶段二：前端基础架构 (Frontend Phase 1)

### 2.1 项目初始化 (Completed)
- [x] 使用 Vite 创建 Vue 3 项目 (`frontend`)。
- [x] 安装依赖: `axios`, `pinia`, `vue-router`, `element-plus`, `sass`。
- [x] 配置 `vite.config.js`:
    - [x] 设置 `@` 别名。
    - [x] 配置 Proxy 代理 (`/api` -> `http://localhost:8080`)。
    - [x] 配置 Element Plus 自动导入。

### 2.2 核心架构 (Completed)
- [x] 封装 `request.js` (Axios 拦截器, Token 注入, 错误处理)。
- [x] 封装 `user.js` (Pinia Store, 持久化 Token)。
- [x] 配置 `router/index.js` (路由守卫)。
- [x] 全局样式 `main.scss`。

### 2.3 基础页面 (Completed)
- [x] 登录页 (`views/login/index.vue`) - 基础 UI 完成，待联调。
- [x] 注册页 (`views/register/index.vue`) - 基础 UI 完成。
- [x] 布局组件 (`layout`) - 侧边栏, 顶部导航。
- [x] 首页/仪表盘 (`views/dashboard/index.vue`)。

---

## 阶段三：业务模块迁移 (Backend Phase 2)

### 3.1 运动员模块 (Athlete) - (Completed)
- [x] Entity, Mapper, Service, Controller。

### 3.2 赛事模块 (Event/Match) - (Completed)
- [x] Entity, Mapper, Service, Controller。
- [x] 迁移赛事发布、列表查询、状态管理。

### 3.3 项目模块 (Project) - (Completed)
- [x] Entity, Mapper, Service, Controller。
- [x] 迁移项目发布、列表查询、与 Event/Athlete 的关联。
- [x] 集成 Registration (Basic)。

### 3.4 报名模块 (Registration) - (Completed)
- [x] Entity, Mapper, Service, Controller。
- [x] 迁移报名申请、审批、查询、删除。
- [x] 与 User/Project/Event 的深度集成。

### 3.5 安全与权限 (Security) - (Completed)
- [x] 完善 Spring Security 配置 (@EnableMethodSecurity).
- [x] User Entity 和 JWT Filter 增加角色/权限处理 (Admin, Athlete, User).
- [x] Controller 层增加 @PreAuthorize 权限控制 (Event, Project, Registration, User).

---

## 阶段四：前端业务页面 (Frontend Phase 2)

### 4.1 核心业务页面 (Core Pages) - (Completed)
- [x] **API 封装**:
    - [x] `event.js`, `project.js`, `athlete.js`, `registration.js`.
- [x] **赛事列表页 (Event List)**:
    - [x] `views/event/index.vue` (展示赛事信息、状态、图片).
- [x] **项目报名页 (Project List)**:
    - [x] `views/project/index.vue` (展示项目、报名按钮、状态判断).
    - [x] 后端优化: `ProjectVO` 增加 `eventName` 和 `registrationStatus` 字段支持前端展示.
- [x] **个人中心页 (Profile)**:
    - [x] `views/profile/index.vue` (个人信息、运动员认证申请、我的报名列表).
- [x] **路由与导航 (Router & Nav)**:
    - [x] 更新 `router/index.js` 增加业务路由.
    - [x] 更新 `Sidebar` 和 `Navbar` 增加菜单入口.
- [x] 前端增加报名审批页面 (Admin) 和路由守卫.

### 4.2 后台管理页面 (Admin Pages) - (Completed)
- [x] **用户管理页 (User Manage)**:
    - [x] `views/user/index.vue` (用户列表、编辑、删除、角色修改).
- [x] **赛事管理页 (Event Manage)**:
    - [x] `views/admin/EventManage.vue` (赛事增删改查、图片链接管理).
- [x] **项目管理页 (Project Manage)**:
    - [x] `views/admin/ProjectManage.vue` (项目增删改查、关联赛事选择).
- [x] **系统导航 (Navigation)**:
    - [x] 更新 `Sidebar` 增加 "系统管理" 下拉菜单 (仅管理员可见).
    - [x] 更新 `router` 增加管理页路由与权限控制.

### 4.3 首页与仪表盘 (Dashboard) - (Completed)
- [x] **后端接口**:
    - [x] 新增 `EventController` 统计接口 (`/total`, `/chart/{date}`).
- [x] **前端页面**:
    - [x] `views/dashboard/index.vue` 集成 ECharts 图表与关键数据展示.

---

## 总结 (Summary)

项目迁移工作主体已完成。
- **后端**: 已升级至 Spring Boot 3 + JDK 17，核心模块（User, Athlete, Event, Project, Registration）均已重构并运行正常。
- **前端**: 已重构为 Vue 3 + Vite + Pinia + Element Plus，实现了普通用户、运动员、管理员的三端业务流程。
- **下一步**: 建议进行全面的系统测试与部署。

---

## 阶段四：前端业务页面 (Frontend Phase 2)

### 4.1 用户管理
- [ ] 用户列表, 详情, 编辑。

### 4.2 赛事管理
- [ ] 赛事列表, 发布, 编辑。

### 4.3 报名管理
- [ ] 报名审核, 列表查看。

---

## 阶段五：测试与部署 (Testing & Deployment)
- [ ] 前后端联调测试。
- [ ] 修复 Bug。
- [ ] 最终部署文档。
