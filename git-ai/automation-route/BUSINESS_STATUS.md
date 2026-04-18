# 当前项目业务现状

本文档基于当前后端控制器源码、README 与现有 smoke/suite 自动化整理，目标是给后续自动化迭代提供稳定的业务地图、测试入口和缺陷优先级。

## 1. 总览

当前项目的主要业务域已经成型，后端接口集中在以下控制器中：

- 系统初始化与学校配置：`SystemInitController`、`AdminSchoolConfigController`
- 用户认证与用户管理：`UserController`、`AdminUserController`
- 运动员申请与赛事管理员审核：`AthleteController`、`EventAdminController`
- 赛事与赛事管理员分配：`EventController`、`AdminEventController`
- 项目与组织架构：`ProjectController`、`DepartmentController`
- 报名：`RegistrationController`
- 成绩：`ScoreController`
- 通知：`NotificationController`
- 管理端统计：`AdminStatsController`

自动化现状：

- 已有稳定回归入口：`dev-start`、`dev-stop`、`smoke-oneclick`、`smoke-suite`
- `smoke-suite` 已覆盖：账号资产自愈、赛事回归窗口归一化、基线 oneclick、当前赛事完整业务链
- 自动化已验证的强链路：
  - 登录
  - 运动员资格已通过场景下的赛事报名
  - 赛事管理员审核报名
  - 学校管理员禁用/启用普通用户后的登录失败与恢复登录
  - 通知未读数与分页一致性
  - 报名结果与成绩查询

自动化尚未系统覆盖的业务：

- 系统初始化/重置的真实接口回放
- 学校配置更新与 logo 上传真实 multipart 链路
- 赛事/项目新增编辑删除的完整管理端链路
- 成绩导入导出、模板下载、成绩发布后的更细分边界
- 管理端统计接口与用户列表筛选的业务断言
- 部门树增删改与组织模式切换后的联动

## 2. 业务域现状

### 2.1 系统初始化与学校配置

主要接口：

- `GET /api/system/init-status`
- `GET /api/system/school-config`
- `POST /api/system/init`
- `PUT /api/admin/school-config`
- `POST /api/admin/school-config/logo`
- `POST /api/admin/school-config/reset`

当前现状：

- 初始化状态检查和学校公开配置接口明确存在，且是当前 smoke 启动健康检查的基础。
- 初始化、重置、学校配置更新已有较完整的后端非容器测试基线，历史 backlog 已覆盖多条边界测试。
- logo 上传目前仍是“占位实现”，服务里返回固定 URL，不依赖真实图床。

测试现状：

- 单测覆盖相对完整。
- 自动化未做真实初始化/重置回放，避免破坏本地现有联调数据。

后续建议：

- 保持以单测为主。
- 若自动化要覆盖这块，优先做独立环境或一次性 reset/init 专用脚本，避免破坏业务回归用数据。

### 2.2 用户认证与用户管理

主要接口：

- `POST /api/auth/login`
- `POST /api/auth/send-verify-code`
- `POST /api/auth/verify-code`
- `POST /api/auth/register`
- `PUT /api/auth/audit/{userId}`
- `GET /api/auth/info`
- `POST /api/auth/reset-password`
- `POST /api/auth/update`
- `POST /api/auth/list`
- `DELETE /api/auth/{id}`
- `GET /api/auth/getUserNumsByMonth`
- `GET /api/auth/getUserType`
- `GET /api/auth/getNums`
- `DELETE /api/auth/logout`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}/role`
- `POST /api/admin/users/{id}/disable`
- `POST /api/admin/users/{id}/enable`

当前现状：

- 注册链路采用 `send-verify-code -> verify-code -> register`，且 heartbeat 回归已经依赖这条链路自动重建 `EVENT_ADMIN / USER / ATHLETE`。
- 用户审核、角色变更、禁用启用接口存在，且当前权限模型允许 `SCHOOL_ADMIN` 完成绝大多数本地联调所需操作。
- 代码与旧接口设计文档存在偏差：
  - 当前实现偏向 `username + password` 登录，而不是仅邮箱登录。
  - 当前公开信息接口和旧设计文档中的 `/api/public/*` 并不一致。

测试现状：

- 自动化已经真实覆盖验证码注册、学校管理员审核、角色变更。
- 自动化现已覆盖禁用/启用与被禁用用户登录失败、重新启用后恢复登录。
- 仍未系统覆盖重置密码、后台用户列表筛选。

后续建议：

- 把“修改角色后的权限变化”加入下一批业务回归。

### 2.3 运动员申请与赛事管理员审核

主要接口：

- `GET /api/athlete/my-applications`
- `POST /api/athlete`
- `GET /api/athlete/apply/{id}`
- `GET /api/athlete/{id}`
- `PUT /api/athlete/{id}`
- `DELETE /api/athlete/{id}`
- `GET /api/event-admin/{eventId}/athlete-applications`
- `POST /api/event-admin/{eventId}/athlete-applications/{applicationId}/approve`
- `POST /api/event-admin/{eventId}/athlete-applications/{applicationId}/reject`
- `POST /api/event-admin/{eventId}/athlete-applications/batch-approve`

当前现状：

- 这条链路是当前自动化账号资产生成的核心前置流程。
- 实际业务依赖“先注册并审核为普通用户，再提交运动员申请，再由赛事管理员审核通过，再把用户角色提升为 ATHLETE”。
- 运动员详情接口带有“按运动员 ID 查询，查不到则退化按 userId/eventId 查询”的兼容逻辑。

测试现状：

- 自动化已经覆盖申请通过主路径。
- 未覆盖拒绝分支、重复申请、更新资料后重提等更细业务。

后续建议：

- 将“reject + reason + 重提”列为优先补测项。

### 2.4 赛事与赛事管理员分配

主要接口：

- `POST /api/event`
- `GET /api/event/page`
- `GET /api/event/newTen`
- `GET /api/event/{id}`
- `DELETE /api/event/{id}`
- `PUT /api/event/{id}`
- `PUT /api/event/{id}/status`
- `GET /api/event/getEventType`
- `GET /api/event/chart/{date}`
- `GET /api/event/total`
- `POST /api/admin/events/{eventId}/admins`
- `DELETE /api/admin/events/{eventId}/admins/{userId}`
- `GET /api/admin/events/{eventId}/admins`
- `POST /api/admin/events/{eventId}/withdraw`

当前现状：

- 赛事详情对 `DRAFT` 状态有限制：普通用户与未绑定赛事管理员不可见。
- 赛事状态机对报名链路有直接影响，当前自动化已经加了本地数据归一化步骤，把目标赛事调整为可回放状态。
- 赛事管理员绑定是 `RequireEventAdmin` 正向路径的前提，自动化会在回归前补绑目标事件。

测试现状：

- 自动化已覆盖状态依赖下的正向链路。
- 自动化已新增 `smoke-event-admin.ps1`，覆盖“创建 `DRAFT` -> 绑定 `EVENT_ADMIN` -> 发布 `OPEN` -> ATHLETE 可见 -> 报名开始后撤回被拒绝 -> 清理回收”的最小管理端闭环。
- 赛事新增、删除、编辑、撤回、管理员列表管理未系统回放。

后续建议：

- 继续向成绩录入/发布闭环推进，并在必要时把赛事管理链路扩到“发布后报名/撤回前后可见性变化”的更细断言。

### 2.5 项目与组织架构

主要接口：

- `GET /api/project/page`
- `POST /api/project`
- `GET /api/project/{id}`
- `GET /api/project/event/{eventId}`
- `DELETE /api/project/{id}`
- `PUT /api/project/{id}`
- `GET /api/department/tree`
- `POST /api/department`
- `PUT /api/department`
- `DELETE /api/department/{id}`

当前现状：

- 项目列表对登录用户会走带“报名状态视角”的查询逻辑。
- 赛事项目列表是当前 `event-workflow` 的关键输入。
- 部门接口会根据当前组织模式自动裁剪字段，但这里的权限注解明显偏弱，增删改接口当前没有显式鉴权。
- 已补齐部门增删改鉴权：`POST/PUT/DELETE /api/department*` 仅允许 `SCHOOL_ADMIN` 调用，并通过定向 smoke 验证 `USER` 越权创建被拒绝（403）。

测试现状：

- 自动化只依赖 `GET /api/project/event/{eventId}` 做报名前置。
- 部门树和项目管理链路尚未进入自动化。

后续建议：

- 优先补一轮部门接口的权限与数据污染风险检查。
- 项目增删改需结合赛事状态一起测。

### 2.6 报名

主要接口：

- `GET /api/registration/page`
- `GET /api/registration/{id}`
- `DELETE /api/registration/{id}`
- `POST /api/registration/apply/{projectId}`
- `PUT /api/registration/attend/{id}?eventId=...`
- `PUT /api/registration/refuse/{id}?eventId=...`
- `PUT /api/registration/batch-audit?approve=...&eventId=...`
- `GET /api/registration/export/{eventId}`
- `GET /api/event-admin/{eventId}/registrations/stats`

当前现状：

- 这是当前业务自动化覆盖最深的模块。
- 历史上已经修复过多轮关键缺陷：
  - 报名与赛事关联断裂
  - 取消/驳回后无法重提
  - `RequireEventAdmin` 参数解析错误
  - 分页与权限正负路径问题
- 现在 `event-workflow` 已稳定覆盖：
  - USER 负向报名
  - ATHLETE 正向报名
  - EVENT_ADMIN 审核通过
  - 审核后报名状态与通知联动

测试现状：

- 主路径覆盖较好。
- 尚未覆盖：
  - 拒绝报名分支
  - 报名导出
  - 报名冲突/人数上限/性别限制/部门限制等细粒度边界

后续建议：

- 报名导出与负向审核是下一优先级。

### 2.7 成绩

主要接口：

- `GET /api/score/page`
- `POST /api/score/upsert`
- `PUT /api/score/{scoreId}`
- `GET /api/score/template/{eventId}`
- `POST /api/score/import/{eventId}`
- `GET /api/score/export/{eventId}`
- `GET /api/score/export-registration/{eventId}`
- `PUT /api/score/publish/{eventId}`
- `GET /api/score/my`
- `GET /api/score/public/{eventId}`

当前现状：

- 成绩模块的公开查询接口已经被 `event-workflow` 覆盖到“我的成绩”和“公开成绩榜”查询可用。
- 但自动化目前没有录入真实成绩，因此更多是在验证接口可达与返回稳定，而不是验证发布后的业务语义。

测试现状：

- 查询覆盖基础可用性。
- 录入、导入、发布、导出尚未形成自动化完整回放。

后续建议：

- 成绩模块应拆成独立测试批次：
  - 确认报名
  - 录入成绩
  - 发布成绩
  - 校验 my/public 查询变化

### 2.8 通知

主要接口：

- `GET /api/notification/page`
- `PUT /api/notification/read/{id}`
- `PUT /api/notification/read-all`
- `GET /api/notification/unread-count`

当前现状：

- 通知模块是当前自动化成熟度最高的模块之一。
- 已系统验证：
  - 分页一致性
  - `read/unread/all` 计数关系
  - 未读数接口
  - 无 token 401
  - 跨用户标记已读 403

测试现状：

- 主功能和核心负向链路已覆盖。

后续建议：

- 仅在报名/成绩/审核链路有变更时做回归。

### 2.9 管理端统计

主要接口：

- `GET /api/admin/stats/overview`
- `GET /api/admin/stats/events`

当前现状：

- 接口存在，但自动化尚未验证统计口径。
- 由于当前本地联调数据会被回归脚本持续修改，这两类统计更适合在固定基线数据集上校验。

测试现状：

- 未覆盖。

后续建议：

- 放入后续低优先级清单，避免在高频 heartbeat 中引入脆弱断言。

## 3. 当前自动化测试重点

heartbeat 自动化后续应优先围绕以下顺序工作：

1. 维持基础可执行性：`dev-start`、账号资产、赛事状态与时间窗口自愈。
2. 维持核心业务主链：
   - 登录
   - 运动员资格有效
   - 项目可见
   - 报名申请
   - 管理员审核
   - 通知增长
   - 报名状态查询
   - 成绩查询
3. 在主链稳定后，逐步扩展到未覆盖业务：
   - 报名拒绝
   - 成绩录入/发布
   - 赛事管理
   - 项目管理
   - 用户禁用/启用与角色变更

## 4. 当前自动化缺陷入口

后续自动化发现 bug 时，优先判断是否落在以下高价值区域：

- 账号资产漂移：种子管理员、业务测试账号、角色绑定失效
- 赛事回归窗口漂移：赛事状态、报名时间、赛事管理员绑定
- 报名状态机：重复报名、取消/拒绝后重提、分页与审核权限
- 通知一致性：unread-count 与 page totals 不一致
- 成绩发布链：录入后不可见、发布后查询不一致

## 4.5 第一批未覆盖链路任务入口

第一批建议直接进入 heartbeat 自动化的任务清单已拆出到：

- `git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md`

当前默认推进顺序：

1. 报名拒绝分支
2. 用户禁用/启用与登录失败校验
3. 赛事管理最小闭环
4. 成绩录入与发布最小闭环
5. 项目/部门接口权限与数据污染检查

当前覆盖进度更新：

- `AUTO-038` 已完成：已覆盖报名拒绝、通知增长、状态查询与同项目重提。
- `AUTO-039` 已完成：已覆盖 `SCHOOL_ADMIN disable/enable USER` 与禁用后登录失败、启用后恢复登录。
- `AUTO-040` 已完成：已覆盖创建 `DRAFT` 赛事、绑定管理员、发布 `OPEN`、`ATHLETE` 对 `DRAFT/OPEN` 可见性切换，以及报名开始后撤回限制；对应脚本入口为 `scripts/smoke-event-admin.ps1`。
- `AUTO-041` 已完成：已覆盖 `score upsert -> publish -> my/public` 最小闭环（并已接入 `smoke-suite` 的 current-event workflow 回放）。
- 当前下一优先级为 `AUTO-042` 项目/部门接口权限边界与数据污染风险检查。

## 5. 与旧接口设计文档的差异

`docs/InterfaceDesign.md` 更像一份理想化设计稿，不完全等于当前代码实现。后续自动化与 bug 判断应以控制器源码和真实行为为准，而不是以该设计稿强行对齐。主要差异包括：

- 当前公开接口并未全面采用 `/api/public/*`
- 当前认证更偏 `username` 登录，而非纯邮箱登录
- 当前接口命名和粒度与设计稿并非一一对应
- 当前权限模型里 `SCHOOL_ADMIN` 在本地联调中承担了大量原本设计上属于 `SUPER_ADMIN` 的操作
