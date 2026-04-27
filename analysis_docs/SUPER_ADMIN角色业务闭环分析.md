# SUPER_ADMIN 角色业务闭环分析报告

> 分析日期：2026-04-26
> 代码基线：master 分支
> 覆盖范围：后端权限体系 + 管理端全量控制器/服务 + 前端管理页面 + 审计日志体系

---

## 一、角色体系中的地位

系统五个角色（`UserRole` 枚举）：

| 角色 | 层级 | 特征 |
|------|------|------|
| **`SUPER_ADMIN`** | **最高** | 系统唯一最高权限，不可被修改/禁用/删除 |
| `SCHOOL_ADMIN` | 高 | 学校范围管理，在权限切面中等同于 SUPER_ADMIN 的赛事管理能力 |
| `EVENT_ADMIN` | 中 | 仅管理被绑定的赛事 |
| `ATHLETE` | 低 | 参赛主体 |
| `USER` | 最低 | 普通注册用户 |

**SUPER_ADMIN 的关键特征：**
- JWT 中自动获得 `ROLE_SUPER_ADMIN` + `ROLE_SCHOOL_ADMIN` 双重 Spring Security 角色
- `@RequireEventAdmin` 切面中硬编码**短路放行**（不查 `event_admin_mapping` 表）
- 前端侧边栏继承 `isSchoolAdmin` + `isEventAdmin` 全部菜单

---

## 二、SUPER_ADMIN 的诞生：系统初始化

### 初始化入口

```
POST /api/system/init
```

- 前端：`/init` 四步向导（学校信息 → 管理员账号 → 组织数据 → 确认提交）
- **完全公开**，无需认证（`SecurityConfig.PUBLIC_PATHS` 包含）
- 幂等性保护：已初始化则返回 409

### 初始化流程（`SchoolConfigServiceImpl.initSystem()`）

```
1. 校验是否已初始化（已初始化 → 409）
2. 创建/更新 SchoolConfig（学校名称、Logo、主题色、联系邮箱）
3. 规范化 orgMode（UNIVERSITY / HIGH_SCHOOL，默认 UNIVERSITY）
4. 创建 SCHOOL_ADMIN 账号（首次登录强制改密）
5. 根据组织模式创建初始年级/院系数据
6. 标记 initialized = true
```

### 创建的管理员

在系统初始化中创建的账号是 **`SCHOOL_ADMIN`**（`initSystem` 方法中 `user.setUserType(UserRole.SCHOOL_ADMIN)`）。

**`SUPER_ADMIN` 不在初始化流程中自动创建**。SUPER_ADMIN 账号的来源需要确认 —— 可能是：
- 数据库初始 SQL 脚本中预置（`resources/sql/data.sql`）
- 或由 SCHOOL_ADMIN 后续手动提升（但当前 `changeRole` 中 SCHOOL_ADMIN 不可被设置为 SUPER_ADMIN）

---

## 三、SUPER_ADMIN 的权限体系

### 3.1 JWT 中的角色注入（`JwtAuthenticationFilter.buildAuthorities()`）

```java
// 通用: ROLE_USER
// SUPER_ADMIN → ROLE_SUPER_ADMIN + ROLE_SCHOOL_ADMIN（双重角色）
// SCHOOL_ADMIN → ROLE_SCHOOL_ADMIN
// EVENT_ADMIN → ROLE_EVENT_ADMIN
// ATHLETE → ROLE_ATHLETE
// USER → 仅 ROLE_USER
```

### 3.2 `@RequireRole` 切面（严格精确匹配）

`PermissionAspect.checkRole()` 直接对比数据库 `user.userType` 与注解声明的角色数组。**无硬编码继承**——SUPER_ADMIN 必须在注解中被显式列出才能通过。

### 3.3 `@RequireEventAdmin` 切面（短路放行）

`PermissionAspect.checkEventAdmin()` 在 **第 82-84 行** 有专门处理：

```java
// SUPER_ADMIN 和 SCHOOL_ADMIN 直接放行，不查 event_admin_mapping 表
if (user.getUserType() == UserRole.SUPER_ADMIN || user.getUserType() == UserRole.SCHOOL_ADMIN) {
    return; // 直接放行
}
```

---

## 四、SUPER_ADMIN 可访问的全量后端端点

### 4.1 明确授权（`@RequireRole({SUPER_ADMIN, SCHOOL_ADMIN})`）

| 控制器 | 端点 | 方法 | 功能 |
|--------|------|------|------|
| `AdminUserController` | `/api/admin/users` | GET | 分页用户列表（按角色/状态/关键词筛选） |
| `AdminUserController` | `/api/admin/users/{id}/role` | PUT | 修改用户角色（仅 USER/EVENT_ADMIN/ATHLETE） |
| `AdminUserController` | `/api/admin/users/{id}/disable` | POST | 禁用用户（SUPER_ADMIN 不可被禁用） |
| `AdminUserController` | `/api/admin/users/{id}/enable` | POST | 启用用户 |
| `AdminStatsController` | `/api/admin/stats/overview` | GET | 总览统计（总用户/赛事/报名/本月新增） |
| `AdminStatsController` | `/api/admin/stats/events` | GET | 各赛事报名统计 |
| `AdminSchoolConfigController` | `/api/admin/school-config` | PUT | 更新学校名称/主题色 |
| `AdminSchoolConfigController` | `/api/admin/school-config/logo` | POST | 上传学校 Logo（≤5MB，仅 jpg/png/gif） |
| `AdminSchoolConfigController` | `/api/admin/school-config/reset` | POST | **重置系统**（清空部门和非管理员用户） |
| `AdminEventController` | `/api/admin/events/{eventId}/admins` | POST | 为赛事指定管理员 |
| `AdminEventController` | `/api/admin/events/{eventId}/admins/{userId}` | DELETE | 移除赛事管理员 |
| `AdminEventController` | `/api/admin/events/{eventId}/admins` | GET | 查看赛事管理员列表 |
| `AdminEventController` | `/api/admin/events/{eventId}/withdraw` | POST | 撤回赛事（状态 → DRAFT） |

### 4.2 同时包含 SUPER_ADMIN + SCHOOL_ADMIN + EVENT_ADMIN

| 控制器 | 端点 | 方法 | 功能 |
|--------|------|------|------|
| `EventController` | `/api/event` | POST | 创建新赛事 |

### 4.3 通过 `@RequireEventAdmin`（切面短路放行）

| 控制器 | 端点 | 方法 | 功能 |
|--------|------|------|------|
| `EventAdminController` | `/api/event-admin/{eventId}/athlete-applications` | GET | 运动员申请列表 |
| `EventAdminController` | `.../athlete-applications/{id}/approve` | POST | 批准运动员申请 |
| `EventAdminController` | `.../athlete-applications/{id}/reject` | POST | 拒绝运动员申请 |
| `EventAdminController` | `.../athlete-applications/batch-approve` | POST | 批量批准运动员申请 |
| `EventAdminController` | `/api/event-admin/{eventId}/registrations/stats` | GET | 报名统计 |
| `RegistrationController` | `/api/registration/attend/{id}` | PUT | 通过报名 |
| `RegistrationController` | `/api/registration/refuse/{id}` | PUT | 拒绝报名 |
| `RegistrationController` | `/api/registration/batch-audit` | PUT | 批量审核报名 |
| `RegistrationController` | `/api/registration/export/{eventId}` | GET | 导出报名名单 |
| `ScoreController` | `/api/score/entry-candidates/{eventId}` | GET | 可录入成绩的已批准报名 |
| `ScoreController` | `/api/score/upsert` | POST | 手动录入/更新成绩 |
| `ScoreController` | `/api/score/{scoreId}` | PUT | 修改成绩（已发布不可改） |
| `ScoreController` | `/api/score/template/{eventId}` | GET | 下载 Excel 导入模板 |
| `ScoreController` | `/api/score/import/{eventId}` | POST | 从 Excel 导入成绩 |
| `ScoreController` | `/api/score/export/{eventId}` | GET | 导出成绩 Excel |
| `ScoreController` | `/api/score/export-registration/{eventId}` | GET | 导出报名名单 Excel |
| `ScoreController` | `/api/score/publish/{eventId}` | PUT | 发布赛事成绩 |
| `EventController` | `/api/event/{id}` | PUT | 更新赛事信息 |
| `EventController` | `/api/event/{id}/status` | PUT | 变更赛事状态 |
| `EventController` | `/api/event/{id}/status-logs` | GET | 赛事状态流转日志 |

### 4.4 注解仅写 `SCHOOL_ADMIN` 但 SUPER_ADMIN **无法**通过 `@RequireRole` 访问

| 控制器 | 端点 | 方法 | 风险 |
|--------|------|------|------|
| `EventController` | `/api/event/{id}` | DELETE | SUPER_ADMIN 理论上被拦截（注解仅 SCHOOL_ADMIN） |
| `ProjectController` | `/api/project` | POST | 同上 |
| `ProjectController` | `/api/project/{id}` | DELETE | 同上 |
| `ProjectController` | `/api/project/{id}` | PUT | 同上 |
| `DepartmentController` | `/api/department` | POST | 同上 |
| `DepartmentController` | `/api/department` | PUT | 同上 |
| `DepartmentController` | `/api/department/{id}` | DELETE | 同上 |

> **注意**：`@RequireRole` 切面做的是精确匹配，不包含角色继承。`AdminSchoolConfigController` 注解为 `{SUPER_ADMIN, SCHOOL_ADMIN}` 可正常访问，但 `DepartmentController` 仅注解了 `{SCHOOL_ADMIN}` —— SUPER_ADMIN **无法**直接创建/修改/删除部门，除非 SCHOOL_ADMIN 的 `buildAuthorities()` 中注入的 `ROLE_SCHOOL_ADMIN` 影响了其他检查路径。然而切面 `checkRole()` 使用的是 `user.getUserType()` 精确匹配，而非 Spring Security 的 `GrantedAuthority`。这意味着 SUPER_ADMIN **无法访问这几个端点**，这是一个潜在的权限盲区。

---

## 五、SUPER_ADMIN 的特殊保护

### 5.1 不可修改角色

```java
// AdminUserServiceImpl.changeRole()
if (targetUser.getUserType() == UserRole.SUPER_ADMIN) {
    throw new BusinessException("禁止修改超级管理员角色", 403);
}
```

### 5.2 不可禁用

```java
// AdminUserServiceImpl.disableUser()
if (targetUser.getUserType() == UserRole.SUPER_ADMIN) {
    throw new BusinessException("禁止禁用超级管理员账号", 403);
}
```

### 5.3 不可删除

```java
// UserServiceImpl.deleteUser()
if (user.getUserType() == UserRole.SUPER_ADMIN) {
    throw new BusinessException("禁止删除超级管理员", 403);
}
```

### 5.4 系统重置时保留

```java
// SchoolConfigServiceImpl.resetSystem()
// 删除所有非 SUPER_ADMIN 且非 SCHOOL_ADMIN 的用户
userMapper.delete(wrapper -> wrapper
    .ne(User::getUserType, UserRole.SUPER_ADMIN)
    .ne(User::getUserType, UserRole.SCHOOL_ADMIN)
);
```

---

## 六、前端管理页面总览

### 侧边栏菜单结构

```
系统首页        /dashboard          ← 所有已认证用户
赛事大厅        /event              ← 所有已认证用户
公开成绩        /public-scores      ← 所有用户
─────────────────────────────────────
赛事管理        /event-manage       ← isEventAdmin (含SUPER)
  创建赛事      /event-create
运动员审核      /athlete-audit       ← isEventAdmin (含SUPER)
报名审核        /registration-audit ← isEventAdmin (含SUPER)
成绩管理        /score-manage       ← isEventAdmin (含SUPER)
─────────────────────────────────────
用户管理        /user-manage        ← isSchoolAdmin (含SUPER)
用户审核        /user-audit         ← isSchoolAdmin (含SUPER)
项目库管理      /project-manage     ← isSchoolAdmin (含SUPER)
学校配置        /school-settings    ← isSchoolAdmin (含SUPER)
```

### 页面功能矩阵

| 页面 | 核心功能 | API 端点 |
|------|----------|----------|
| `/dashboard` | KPI 卡片（用户/赛事/报名/新增）+ 赛事状态分布 + 各赛事统计 | `GET /admin/stats/overview`, `GET /admin/stats/events` |
| `/user-manage` | 用户筛选（角色/状态/关键词）、修改角色、禁用/启用 | `GET/PUT /admin/users/*` |
| `/user-audit` | 审核注册用户（通过/拒绝，拒绝需填写原因） | `PUT /api/auth/audit/{userId}` |
| `/event-manage` | 赛事编辑/删除/状态变更/分配管理员/导入成绩/发布成绩 | `GET/PUT/DELETE /event/*`, `POST /admin/events/*` |
| `/event-create` | 三步向导：基本信息 → 项目配置 → 指定管理员 | `POST /event`, `PUT /event/{id}/status` |
| `/athlete-audit` | 按赛事审核运动员申请（通过/拒绝/批量通过） | `GET/POST /event-admin/*` |
| `/registration-audit` | 报名审核（通过/拒绝/批量审核） | `GET/PUT /registration/*` |
| `/score-manage` | 手动录入成绩/编辑/导入Excel/导出/发布 | `GET/POST/PUT /score/*` |
| `/project-manage` | 项目库 CRUD | `GET/POST/PUT/DELETE /project/*` |
| `/school-settings` | 学校名称/Logo/主题色配置 + 系统重置 | `PUT/POST /admin/school-config/*` |

**SUPER_ADMIN 独有功能：**
- `/event-manage` 中的"分配管理员"按钮**仅 SUPER_ADMIN 可见**（`EventManage.vue:85` 处 `v-if="isSuperAdmin"`）

---

## 七、审计日志体系

### 7.1 AdminUserAuditLog（管理操作审计）

| 操作类型 | 记录位置 | 记录内容 |
|----------|----------|----------|
| `ROLE_CHANGE` | `AdminUserServiceImpl.changeRole()` | beforeRole → afterRole，beforeStatus，afterStatus |
| `ROLE_CHANGE` | `EventServiceImpl.saveRoleAuditLog()` | EVENT_ADMIN 自动升级（赛事管理员分配） |
| `STATUS_CHANGE` | `AdminUserServiceImpl.disableUser()` | beforeStatus → DISABLED |
| `STATUS_CHANGE` | `AdminUserServiceImpl.enableUser()` | beforeStatus → ACTIVE |
| `LOGICAL_DELETE_USER` | `UserServiceImpl.saveDeleteAuditLog()` | 逻辑删除用户 |

每条记录包含：`operatorId`、`targetUserId`、`action`、`beforeRole`、`afterRole`、`beforeStatus`、`afterStatus`、`remark`。

### 7.2 ScoreAuditLog（成绩变更审计）

| 触发条件 | 记录内容 |
|----------|----------|
| 成绩值/排名/备注变化 | beforeValue → afterValue，beforeRank → afterRank，beforeRemark → afterRemark |

- 仅在 `hasScoreChanged()` 为 true 时写入
- 包含 `operatorId` 追踪操作人

### 7.3 EventStatusOperationLog（赛事状态流转审计）

| 触发条件 | 记录内容 |
|----------|----------|
| 赛事状态变更（发布/撤回/结束等） | fromStatus → toStatus，operationType，triggerSource（MANUAL/SYSTEM/SCHEDULED），reason |

- 分页可查询（`EventServiceImpl.getEventStatusOperationLogs()`），含操作人姓名解析

---

## 八、业务闭环分析

### 8.1 SUPER_ADMIN 核心业务覆盖

| 业务领域 | 操作 | 状态 |
|----------|------|------|
| 系统初始化 | 引导创建学校 + 管理员 + 组织数据 | ✅ 完整 |
| 用户管理 | 列表/筛选/角色变更/禁用/启用/删除 | ✅ 完整 |
| 用户审核 | 审核注册用户（通过/拒绝含原因） | ✅ 完整 |
| 赛事管理 | 创建/编辑/删除/状态流转/撤回 | ✅ 完整 |
| 赛事管理员 | 指定/移除管理员（**仅 SUPER_ADMIN**） | ✅ 完整 |
| 项目管理 | 创建/编辑/删除项目库 | ⚠️ 见下文 |
| 运动员审核 | 审核运动员申请（通过/拒绝/批量） | ✅ 完整 |
| 报名审核 | 审核项目报名（通过/拒绝/批量） | ✅ 完整 |
| 成绩管理 | 录入/编辑/导入/导出/发布 | ✅ 完整 |
| 学校配置 | 名称/Logo/主题色修改 | ✅ 完整 |
| 系统重置 | 清空非管理员数据，重新初始化 | ✅ 完整 |
| 统计看板 | KPI + 赛事分布 + 各赛事统计 | ✅ 完整 |
| 审计追踪 | 用户管理操作/成绩变更/赛事状态流转 | ✅ 完整 |

### 8.2 闭环评估：已具备的能力

- **用户全生命周期管理**：审核 → 角色变更 → 禁用/启用 → 逻辑删除，全程审计
- **赛事全生命周期管理**：创建 → 发布 → 进行中 → 结束，状态流转带日志
- **管理员委派**：SUPER_ADMIN 独有分配/移除赛事管理员权限
- **数据安全**：SUPER_ADMIN 账号三重保护（不可改角色/不可禁用/不可删除），系统重置保留

### 8.3 闭环评估：存在问题

| 问题 | 严重程度 | 详情 |
|------|----------|------|
| **SUPER_ADMIN 可能无法操作部门和项目** | 🔴 中 | `DepartmentController` 创建/更新/删除仅注解 `@RequireRole({SCHOOL_ADMIN})`，`ProjectController` 同上。`@RequireRole` 切面精确匹配 `userType`，SUPER_ADMIN 不等于 SCHOOL_ADMIN，将被 403 拒绝。前端虽然显示了菜单入口，但后端请求会失败 |
| **SUPER_ADMIN 可能无法删除赛事** | 🔴 中 | `EventController.deleteEvent()` 仅注解 `@RequireRole({SCHOOL_ADMIN})` |
| **SUPER_ADMIN 账号来源不明确** | 🟡 低 | 系统初始化创建的是 SCHOOL_ADMIN，SUPER_ADMIN 如何产生需要确认（可能是 SQL 预置） |
| **角色提升范围受限** | 🟡 低 | `AdminUserServiceImpl.changeRole()` 限制目标角色仅 `USER`/`EVENT_ADMIN`/`ATHLETE`，无法设置 SCHOOL_ADMIN 或 SUPER_ADMIN |
| **审计日志只写不读** | 🟡 低 | `AdminUserAuditLog` 和 `ScoreAuditLog` 有写入但未见查询端点，审计数据无法在前端展示 |
| **禁用后 Token 失效但启用不恢复** | 🟢 低影响 | `disableUser()` 删除 Redis Token，`enableUser()` 不恢复。用户需重新登录，合理行为 |

### 8.4 权限注解不一致清单

以下是注解范围与 SUPER_ADMIN 预期权限存在不一致的端点：

| 端点 | 当前注解 | SUPER_ADMIN 预期 | 实际结果 |
|------|----------|------------------|----------|
| `DELETE /api/event/{id}` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `POST /api/project` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `PUT /api/project/{id}` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `DELETE /api/project/{id}` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `POST /api/department` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `PUT /api/department` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |
| `DELETE /api/department/{id}` | `@RequireRole({SCHOOL_ADMIN})` | 应能访问 | ❌ 403 |

**根因**：`@RequireRole` 切面只做精确的 `userType` 匹配，而 `JwtAuthenticationFilter.buildAuthorities()` 虽然为 SUPER_ADMIN 注入了 `ROLE_SCHOOL_ADMIN`，但这个 Spring Security 权限**并未被 `PermissionAspect.checkRole()` 使用**。

---

## 九、修复建议

### P0：修复权限注解不一致

将以下控制器的 `@RequireRole` 注解统一加入 `SUPER_ADMIN`：

```java
// DepartmentController
@RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})

// ProjectController (create/update/delete)
@RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})

// EventController.deleteEvent
@RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
```

或在 `PermissionAspect.checkRole()` 中加入角色继承逻辑（与 `buildAuthorities()` 对齐）：
```java
// 在 checkRole() 方法开头添加
if (user.getUserType() == UserRole.SUPER_ADMIN) {
    return; // SUPER_ADMIN 全局放行
}
```

### P1：确认 SUPER_ADMIN 账号来源

检查 `resources/sql/data.sql` 中是否预置了 SUPER_ADMIN 账号，或在初始化流程中增加 SUPER_ADMIN 创建逻辑。

### P2：审计日志查询界面

为 `AdminUserAuditLog` 和 `ScoreAuditLog` 增加查询端点和管理界面入口，让审计数据可被消费。

---

## 十、结论

**SUPER_ADMIN 角色的核心管理业务已基本完整**：赛事全生命周期管理、用户全生命周期管理、成绩管理、系统配置和重置、管理员委派均具备完整的前后端支持。审计日志体系覆盖了用户管理操作、成绩变更和赛事状态流转三类关键操作。

**核心风险在于权限注解不一致**：`@RequireRole` 缺少角色继承机制，导致 7 个端点对 SUPER_ADMIN 不可达。这是从代码层面可见的问题，前端菜单虽然显示完整，但实际请求会收到 403。建议优先修复 `PermissionAspect.checkRole()` 加入 SUPER_ADMIN 全局放行逻辑，一次性解决所有注解遗漏问题。
