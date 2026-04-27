# ATHLETE 角色业务闭环分析报告

> 分析日期：2026-04-26
> 代码基线：master 分支
> 覆盖范围：后端控制器/服务/实体 + 前端页面/路由/API模块

---

## 一、角色体系概述

系统定义五个角色（`UserRole` 枚举）：

| 角色 | 权限特征 |
|------|----------|
| `SUPER_ADMIN` | 超级管理员，继承 SCHOOL_ADMIN 权限 |
| `SCHOOL_ADMIN` | 学校管理员，管理本校所有资源 |
| `EVENT_ADMIN` | 赛事管理员，仅管理被绑定的赛事 |
| **`ATHLETE`** | **运动员，参赛主体** |
| `USER` | 普通注册用户（未成为运动员） |

**ATHLETE 的权限边界（通过 `PermissionAspect` + `@RequireRole` 控制）：**
- 仅能访问 `@RequireRole({ATHLETE, ...})` 标注的端点
- 无法访问 `@RequireRole({SUPER_ADMIN})`、`@RequireRole({SCHOOL_ADMIN})`、`@RequireRole({EVENT_ADMIN})` 端点
- 无法访问 `@RequireEventAdmin` 端点（切面中的宽限仅对 SUPER_ADMIN/SCHOOL_ADMIN 放行）
- `@RequireRole` 对数据库 `user.user_type` 字段做**精确匹配**，无角色继承

---

## 二、如何成为 ATHLETE（完整链路）

### 步骤 1：注册账号

```
POST /api/auth/send-verify-code → POST /api/auth/verify-code → POST /api/auth/register
```

- 前端：`/register` 三步向导（输入邮箱 → 验证码 → 设置用户名密码）
- 注册后账号状态：`userType = USER`，`status = PENDING`
- **阻塞点：** 注册后的 USER 账号处于 PENDING 状态，未经管理员审核（`PUT /api/auth/audit/{userId}`）无法登录使用

### 步骤 2：完善个人资料

- 端点：`POST /api/auth/update`
- 前端：`/profile` 页面的"个人资料"区域
- **必须填写的字段：** `name`、`gender`、`contact`、`deptId`（院系/班级）
- **阻断机制：** 后端 `AthleteServiceImpl.add()` 强制校验这四个字段，任何缺失都会返回错误：`"Please complete identity info..."`

### 步骤 3：提交运动员认证申请

```
POST /api/athlete
```

- 前端入口：`/profile` → "运动员认证" Tab → 选择赛事 → 提交
- 后端 `AthleteServiceImpl.add()` 的业务规则：
  - 校验 `eventId` 非空
  - 用户不能是 `SUPER_ADMIN` 或 `EVENT_ADMIN`
  - 必须完成个人资料（步骤2的四个字段）
  - **重复申请检查：**
    - 同一 userId + eventId，已有 PENDING → 拒绝（"已有待审核申请"）
    - 已有 APPROVED → 拒绝（"已通过运动员资格"）
    - 已有 REJECTED → **允许**删除旧记录后重新提交
  - 创建 `Athlete` 记录（`athleteState = PENDING`），从 User 表拷贝姓名/性别/联系方式/部门

### 步骤 4：管理员审核

- 管理员端点：`POST /api/admin/athlete/approve/{eventId}/{applicationId}`
- 通过 → `athleteState = APPROVED` + `agreeTime` 记录 + 发送 `ATHLETE_APPROVED` 通知
- 拒绝 → `athleteState = REJECTED` + 发送 `ATHLETE_REJECTED` 通知（含原因）

### 步骤 5：角色升级

- 当运动员申请被批准后，用户的 `userType` 应当更新为 `ATHLETE`
- JWT 在下次登录时携带 `role: "ATHLETE"`
- `JwtAuthenticationFilter.buildAuthorities()` 为其赋予 `ROLE_USER` + `ROLE_ATHLETE`

> **注意：** 步骤 5 的角色更新逻辑在当前 `AthleteServiceImpl.approveAthleteApplication()` 中需要确认是否正确执行了 `user.setUserType(UserRole.ATHLETE)` 并保存。这关系到 ATHLETE 能否在不下线重登的情况下访问受 `@RequireRole(ATHLETE)` 保护的端点。

---

## 三、如何参加赛事项目（完整链路）

### 步骤 1：浏览赛事

```
GET /api/event/page
```

- 前端：`/event` 赛事大厅页面，支持名称搜索和分页
- **无需认证即可访问**（公开端点）

### 步骤 2：查看赛事详情与项目列表

```
GET /api/event/{id}          → 赛事基本信息
GET /api/athlete/apply/{userId}?eventId=  → 运动员资格状态
GET /api/project/event/{eventId}           → 项目列表（含名额余量）
GET /api/registration/page                 → 已有报名记录
```

- 前端：`/event/:id` 详情页
- 页面根据运动员状态和报名窗口期动态控制按钮状态

### 步骤 3：报名项目

```
POST /api/registration/apply/{projectId}
```

- 前端：赛事详情页项目中点击"立即报名"
- **后端 `RegistrationServiceImpl.add()` 的 7 层防护**：

| 序号 | 防护层 | 机制 |
|------|--------|------|
| 1 | 幂等性防抖 | Redis 5秒桶 `registration:idempotency:userId:projectId` |
| 2 | 分布式锁 | Redisson 两级锁（user_event + project），10秒租约 |
| 3 | 用户状态 | user.status == ACTIVE，且不是 SUPER_ADMIN/EVENT_ADMIN |
| 4 | 运动员资格 | 当前赛事存在 APPROVED 状态的 Athlete 记录 |
| 5 | 数据库行锁 | `lockAthleteRowForEvent()` 防止 max-items 竞态 |
| 6 | 赛事窗口校验 | event.status == OPEN，当前时间在报名窗口内 |
| 7 | 业务规则校验 | 性别限制、部门限制、人数上限、时间冲突、最大报名数 |

- 报名成功后：创建 `Registration`（status = PENDING）
- 自动异步同步运动员档案到 User 表（`syncAthleteProfileToUser`）

### 步骤 4：管理员审核报名

| 端点 | 操作 |
|------|------|
| `PUT /api/registration/attend/{id}` | 通过 |
| `PUT /api/registration/refuse/{id}` | 拒绝 |
| `PUT /api/registration/batch-audit` | 批量审核 |

- 审核后发送 SYSTEM 通知给运动员

### 步骤 5：自行取消报名

```
DELETE /api/registration/{id}
```

- 仅限本人操作（athleteId 校验）
- 窗口期校验（赛事 CLOSED 时可放宽）
- 取消后项目名额自动释放（`decrementAttendance()`）
- 被拒/已取消的记录可被复用重新报名

---

## 四、如何查看自己的成绩

### 个人成绩查询

```
GET /api/score/my
```

- 前端：`/my-score` 页面
- **仅返回已发布成绩**（`isPublished = true`）
- 可按赛事筛选，按发布时间和排名排序
- 显示：赛事名、项目名、运动员名、成绩值、排名、发布时间

### 公开成绩榜

```
GET /api/score/public/{eventId}
```

- 前端：`/public-scores` 页面
- **无需认证**，按项目分组展示已发布成绩

### 成绩录入流程（由管理员完成）

```
POST /api/score/upsert              → 录入/更新成绩（仅 APPROVED/CONFIRMED 报名）
POST /api/score/import/{eventId}    → Excel 批量导入（BEST_EFFORT / STRICT 两种模式）
PUT  /api/score/publish/{eventId}   → 一键发布赛事所有未发布成绩
```

- 已发布成绩不可修改
- 发布后自动向所有参赛运动员发送 `RESULT_PUBLISHED` 通知
- 成绩变更记录在 `ScoreAuditLog`（前后值快照）

---

## 五、ATHLETE 可访问的前端路由与 API 汇总

### 前端路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/dashboard` | 首页 | 数据看板 |
| `/event` | 赛事探索 | 赛事卡片列表 |
| `/event/:id` | 赛事详情 | 项目浏览/报名/取消 |
| `/public-scores` | 公开成绩 | 无需认证即可访问 |
| `/profile` | 个人中心 | 资料编辑、运动员认证申请 |
| `/my-registrations` | 我的报名 | 报名记录查看/取消 |
| `/my-applications` | 我的申请 | 运动员申请状态跟踪 |
| `/my-score` | 我的成绩 | 已发布个人成绩 |
| `/notifications` | 消息通知 | 站内通知列表 |

### API 端点（运动员侧）

| HTTP | 端点 | 用途 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/send-verify-code` | 发送验证码 |
| POST | `/api/auth/verify-code` | 校验验证码 |
| DELETE | `/api/auth/logout` | 登出 |
| GET | `/api/auth/info` | 获取个人信息 |
| POST | `/api/auth/update` | 更新个人资料 |
| GET | `/api/athlete/my-applications` | 我的运动员申请列表 |
| POST | `/api/athlete` | 提交运动员申请 |
| GET | `/api/athlete/apply/{userId}` | 查询特定赛事资格状态 |
| DELETE | `/api/athlete/{id}` | 取消运动员申请 |
| GET | `/api/event/page` | 赛事列表 |
| GET | `/api/event/{id}` | 赛事详情 |
| GET | `/api/project/event/{eventId}` | 赛事项目列表 |
| POST | `/api/registration/apply/{projectId}` | 报名项目 |
| GET | `/api/registration/page` | 我的报名列表 |
| DELETE | `/api/registration/{id}` | 取消报名 |
| GET | `/api/score/my` | 我的成绩 |
| GET | `/api/score/public/{eventId}` | 公开成绩榜 |
| GET | `/api/notification/**` | 通知相关 |

---

## 六、业务闭环分析

### 6.1 正向主链路状态转换

```
匿名访问者
   │
   ▼
[注册] → 邮箱验证 → 设置账号
   │
   ▼
[USER / PENDING]  ←← 管理员审核通过 → [USER / ACTIVE]
   │
   ▼
[完善个人资料] → 填写 name/gender/contact/deptId
   │
   ▼
[提交运动员申请] → PENDING
   │
   ├─ 管理员通过 → [ATHLETE / APPROVED]  ← 获得参赛资格
   │
   └─ 管理员拒绝 → [REJECTED] → 可删除后重新提交
   │
   ▼
[浏览赛事] → 进入详情
   │
   ▼
[报名项目] → PENDING
   │
   ├─ 管理员通过 → [APPROVED] / [CONFIRMED]
   │
   ├─ 管理员拒绝 → [REJECTED] → 可复用记录重新报名
   │
   └─ 自行取消 → [CANCELLED] → 可复用记录重新报名
   │
   ▼
[成绩录入] ← 管理员操作
   │
   ▼
[成绩发布] → 运动员可在 /my-score 查看
            → 公开榜可查看
```

### 6.2 闭环评估：已具备的完整链路

| 环节 | 状态 | 说明 |
|------|------|------|
| 注册→登录 | ✅ 完整 | 邮箱验证码注册 + JWT 登录 + 首次改密 |
| 账号审核 | ✅ 完整 | 管理员审核注册用户 |
| 个人资料完善 | ✅ 完整 | 含强制字段校验阻断 |
| 运动员申请 | ✅ 完整 | PENDING/APPROVED/REJECTED 全状态覆盖 |
| 取消运动员申请 | ✅ 完整 | 仅无报名记录时可取消 |
| 重新申请 | ✅ 完整 | REJECTED 后可删除重新提交 |
| 赛事浏览 | ✅ 完整 | 公开接口，无需登录 |
| 赛事详情+项目列表 | ✅ 完整 | 显示名额余量 + 运动员资格状态 |
| 项目报名 | ✅ 完整 | 7 层防护（幂等/锁/校验/竞态） |
| 报名审核 | ✅ 完整 | 单条+批量，带通知 |
| 取消报名 | ✅ 完整 | 本人操作 + 窗口期校验 + 名额回退 |
| 重新报名 | ✅ 完整 | CANCELLED/REJECTED 记录可复用 |
| 成绩录入 | ✅ 完整 | 手动/批量导入，带审计日志 |
| 成绩发布 | ✅ 完整 | 一键发布 + 通知推送 |
| 个人成绩查看 | ✅ 完整 | 仅已发布，支持赛事筛选 |
| 公开成绩榜 | ✅ 完整 | 无需认证，按项目分组 |

### 6.3 闭环评估：缺失或待完善的点

| 问题 | 严重程度 | 详情 |
|------|----------|------|
| **角色未自动升级** | 🔴 中 | `AthleteServiceImpl.approveAthleteApplication()` 是否将 `user.userType` 从 USER 更新为 ATHLETE 需确认。否则用户需要重新登录才能获得 ATHLETE 角色对应的 JWT 权限 |
| **运动员申请状态变更无通知** | 🟡 低 | 后端发送了 ATHLETE_APPROVED/ATHLETE_REJECTED 通知，前端通知中心可查看，但详情页无主动推送 |
| **报名后的运动员侧无确认/拒绝原因展示** | 🟡 低 | `/my-registrations` 页显示状态但可能不显示 `rejectReason`，需确认 |
| **成绩查看仅限已发布** | 🟢 设计如此 | 运动员无法查看自己的未发布成绩，这是合理的业务设计 |
| **运动员申请被拒后无原因展示** | 🟡 低 | 前端 `/my-applications` 页的拒绝记录卡片需要确认是否展示了拒绝原因 |
| **赛事报名截止后无法取消报名** | 🟢 合理 | 窗口期结束后禁止取消是合理的，特殊情况下需联系管理员 |
| **跨赛事的运动员资格不互通** | 🟢 设计如此 | 每个赛事需独立申请运动员资格，符合业务场景 |
| **部门更换需重新审核** | 🟢 合理 | 已通过运动员修改 deptId 需联系管理员，防止绕过部门限制 |

### 6.4 边界与异常场景覆盖

| 场景 | 处理情况 |
|------|----------|
| 同一赛事重复提交运动员申请 | ✅ PENDING 时拒绝，REJECTED 后允许 |
| 同一项目重复报名 | ✅ 非 CANCELLED/REJECTED 记录时拒绝 |
| 5秒内重复提交报名 | ✅ Redis 幂等桶防抖 |
| 并发报名最后一个名额 | ✅ DB 原子 increment + Redisson 锁 |
| 被拒后重新报名 | ✅ 复用旧记录，状态重置为 PENDING |
| 取消后重新报名 | ✅ 复用旧记录，状态重置为 PENDING |
| 报名超过个人单项上限 | ✅ event.maxItemsPerAthlete 校验 |
| 项目时间冲突 | ✅ findConflictItemName SQL 检测 |
| 性别不符 | ✅ genderLimit 枚举校验 |
| 部门/年级不符 | ✅ limitDeptIds JSON 匹配（含祖先层级匹配） |
| 被禁用用户报名 | ✅ user.status == ACTIVE 校验 |
| 赛事非 OPEN 状态报名 | ✅ event.status == OPEN 校验 |
| 报名窗口期外报名 | ✅ 时间范围校验 |
| 管理员/超级管理员冒充运动员 | ✅ 角色拦截 |
| 取消他人报名 | ✅ athleteId 校验（返回 403） |
| 修改已发布成绩 | ✅ isPublished 校验（拒绝修改） |
| 无报名记录时录入成绩 | ✅ Registration 状态校验 |

---

## 七、架构特点与风险

### 优点

1. **多层并发防护**：Redis 幂等 + Redisson 分布式锁 + DB 行锁 + 原子递增，防止报名超卖
2. **状态机完整**：所有业务实体（Athlete/Registration/Event/Score）都有清晰的状态枚举和转换路径
3. **记录可复用**：CANCELLED/REJECTED 的报名记录支持原地复用，避免了唯一键冲突
4. **权限边界清晰**：通过 `@RequireRole` + `@RequireEventAdmin` 两层注解 + PermissionAspect 切面，隔离了角色权限

### 需要注意

1. **`@RequireRole` 无角色继承**：例如标注 `@RequireRole({ATHLETE})` 时，SUPER_ADMIN 也无法访问。如果需要管理员也能访问运动员接口，注解需要显式加入管理员角色
2. **JWT 角色刷新**：用户角色升级后（USER→ATHLETE），需要重新登录才能获得新权限，因为角色信息写入 JWT
3. **前端角色判断**：前端 `normalizeUserInfo` 处理了 `role`/`type`/`userType` 三个字段名的不一致，但这暗示后端返回的字段名存在不统一

---

## 八、结论

**ATHLETE 角色的核心业务已形成完整闭环**：从注册 → 资料完善 → 运动员认证 → 赛事报名 → 成绩查看，每一步都有状态管理、权限校验和异常处理。

三个建议优先级：
1. **P1**：确认 `approveAthleteApplication()` 中是否正确执行了 `userType` 更新为 `ATHLETE`
2. **P2**：前端 `/my-applications` 和 `/my-registrations` 的拒绝记录增加拒绝原因展示
3. **P3**：统一后端用户信息返回字段名（role/type/userType），消除前端 normalizeUserInfo 的兼容负担
