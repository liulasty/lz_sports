# 第一批未覆盖业务链路自动化测试任务清单

本文档基于 `git-ai/automation-route/BUSINESS_STATUS.md` 提炼第一批适合立即进入 heartbeat 自动化迭代的业务链路。选择标准：

- 对现有主回归价值高
- 能在当前本地联调环境稳定复跑
- 失败后有明确 bug 修复落点
- 不需要大规模重置整库或引入独立环境

## 批次目标

在不破坏现有 `suite` 主链稳定性的前提下，先扩展 5 条未覆盖业务链路：

1. 报名拒绝分支
2. 用户禁用/启用与登录失败校验
3. 赛事管理最小闭环
4. 成绩录入与发布最小闭环
5. 项目/部门接口的权限与数据污染检查

## 任务清单

### AUTO-038 报名拒绝分支

目标：

- 在现有角色资产下补覆盖 `EVENT_ADMIN reject registration`
- 验证拒绝后：
  - 报名状态为 `REJECTED`
  - 运动员通知增长
  - 后续允许重新报名或至少不破坏主链

关键接口：

- `PUT /api/registration/refuse/{id}?eventId=...`
- `GET /api/registration/page`
- `GET /api/notification/page`
- `GET /api/notification/unread-count`

通过标准：

- 定向 smoke 可稳定复跑
- 不影响现有 `suite` 主链

### AUTO-039 用户禁用/启用与登录失败校验

当前状态：

- DONE（2026-04-18）
- 已通过定向 `smoke-user-status.ps1` 覆盖 `disable -> 登录失败(409) -> enable -> 登录恢复`

目标：

- 覆盖学校管理员禁用/启用普通用户
- 覆盖被禁用用户登录失败、启用后恢复登录
- 验证角色/状态变化不破坏现有账号资产生成逻辑

关键接口：

- `POST /api/admin/users/{id}/disable`
- `POST /api/admin/users/{id}/enable`
- `POST /api/auth/login`
- `GET /api/admin/users`

通过标准：

- 禁用后登录返回稳定业务错误
- 启用后可再次登录成功

### AUTO-040 赛事管理最小闭环

当前状态：

- DONE（2026-04-18）
- 已通过定向 `smoke-event-admin.ps1` 覆盖“创建 `DRAFT` -> 绑定 `EVENT_ADMIN` -> 发布 `OPEN` -> 报名开始后撤回受限 -> 清理删除”

目标：

- 新建一个最小 `DRAFT` 赛事
- 指定赛事管理员
- 发布为 `OPEN`
- 验证报名窗口依赖和撤回限制

关键接口：

- `POST /api/event`
- `POST /api/admin/events/{eventId}/admins`
- `PUT /api/event/{id}/status?status=OPEN`
- `POST /api/admin/events/{eventId}/withdraw`
- `GET /api/event/{id}`

通过标准：

- `DRAFT -> OPEN` 正向可用
- 报名开始后撤回限制符合当前实现

### AUTO-041 成绩录入与发布最小闭环

目标：

- 在已有报名记录上完成成绩录入
- 发布成绩
- 验证 `my/public` 查询出现对应变化

关键接口：

- `POST /api/score/upsert`
- `PUT /api/score/publish/{eventId}`
- `GET /api/score/my`
- `GET /api/score/public/{eventId}`

通过标准：

- 发布前后查询结果差异明确
- 不引入已发布成绩不可修改之外的新回归

### AUTO-042 项目/部门接口权限与数据污染检查

目标：

- 检查部门接口增删改是否存在越权风险
- 检查项目接口在非管理员角色下的访问边界
- 对明显不合理的权限或数据污染问题形成缺陷记录或直接修复

关键接口：

- `GET /api/department/tree`
- `POST /api/department`
- `PUT /api/department`
- `DELETE /api/department/{id}`
- `POST /api/project`
- `PUT /api/project/{id}`
- `DELETE /api/project/{id}`

通过标准：

- 至少形成 1 条清晰的权限或污染风险验证结果
- 若发现 bug，优先补最小修复和回归验证

## 执行顺序

建议 heartbeat 自动化按以下顺序推进：

1. `AUTO-038` 报名拒绝分支
2. `AUTO-039` 用户禁用/启用
3. `AUTO-040` 赛事管理最小闭环
4. `AUTO-041` 成绩录入与发布
5. `AUTO-042` 项目/部门权限检查

原因：

- 前两项最接近当前已有主链，改动半径最小
- 中间两项能逐步把“管理端到业务结果”的闭环补完整
- 最后一项更偏探索性和风险排查，适合放在主链外缘

当前推进结果：

- `AUTO-038` 已完成
- `AUTO-039` 已完成
- `AUTO-040` 已完成
- `AUTO-041` 已完成：已覆盖 `score upsert -> publish -> my/public` 最小闭环（已接入 `smoke-suite` 的 current-event workflow 回放）
- 下一条建议执行 `AUTO-042`

## 与 heartbeat 的关系

heartbeat 自动化后续应先读取本文档，再与 `BUSINESS_STATUS.md` 联合决定当前最优先的未覆盖业务链路。若本批任务全部完成，再启动第二批清单。
