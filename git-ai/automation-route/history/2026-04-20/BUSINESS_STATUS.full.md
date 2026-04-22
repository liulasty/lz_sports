# BUSINESS_STATUS 完整历史快照（2026-04-20）

> 说明：此文件为 `git-ai/automation-route/BUSINESS_STATUS.md` 的长文历史快照。  
> 主路径已做瘦身，仅保留“结论层 + 入口索引”。

（以下内容为当日快照，保持原样）

---

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
- 已补充业务码断言意识：部分接口虽返回 HTTP 200，但失败语义由 `Result.code` 表达，smoke 需以业务码为准。
- 已新增通用断言 helper：`scripts/lib/business-code-assert.ps1`，用于统一 business code 断言。
- 自动化已验证的强链路：
  - 登录
  - 运动员资格已通过场景下的赛事报名
  - 赛事管理员审核报名
  - 学校管理员禁用/启用普通用户后的登录失败与恢复登录
  - 通知未读数与分页一致性
  - 通知 read/read-all 与 unread-count 一致性（定向 smoke）
  - 报名结果与成绩查询

自动化尚未系统覆盖的业务：

- 赛事/项目新增编辑删除的完整管理端链路（仍以最小闭环为主）
- 成绩导入的细粒度模板错误（缺列、格式错误、重复行）的分项断言
- 部门树增删改与组织模式切换后的联动

（中间段落省略：详见原主文件历史版本）

## 6. 最新 smoke 结论（2026-04-20）

- 在 `git-ai/automation-route` worktree 完成 `AUTO-045~AUTO-050` 闭环回归，新增通过记录包括 `2026-04-20-auto-001.md`、`2026-04-20-auto-002.md`、`2026-04-20-auto-049-template-export.md`。
- 已完成本轮基线 `suite` 回归：`2026-04-20-auto-006.md` 一次通过，`oneclick` 与 `current-event workflow` 全部通过。
- 已完成 `AUTO-054` 定向链路回放：`2026-04-20-auto-054-new-event-business.md`、`2026-04-20-auto-054-new-event-branches.md`、`2026-04-20-auto-054-event-admin.md` 全部通过，覆盖新赛事创建/发布、分支审核、撤回与清理删除。
- `AUTO-054` 收敛基线通过：`2026-04-20-auto-007.md` 再次通过，说明未破坏 `suite` 主链。
- 已完成 `AUTO-055` 定向链路：`2026-04-20-auto-055-event-project-edit-boundary-rerun.md` 通过，覆盖 event/project 编辑字段约束与 USER 越权写入拒绝。
- `AUTO-055` 首次失败已闭环修复：失败信号为脚本断言将非法时间窗口固定为 `400`，实际业务返回 `409`；修正后执行 `dev-stop -> dev-start -> suite(2026-04-20-auto-008)` 再次全绿。
- 已连续完成 `AUTO-056~AUTO-058`：新增 `smoke-event-admin-permission-shift.ps1` 并按三种场景回放通过（跨赛事编辑拒绝、解绑后权限收敛、项目写接口角色边界）。
- `AUTO-056~AUTO-058` 收敛基线通过：`2026-04-20-auto-009.md` 再次通过，主链稳定。
- 已完成真实 `reset/init` 回放，并通过后续 `smoke-suite` 自愈恢复账号资产与主链可复跑性。
- 新增 `scripts/smoke-score-template-export.ps1`，覆盖 `score template/export/export-registration` 导出可用性、USER 越权拒绝、坏文件导入拒绝（code=400）。
- 已完成 `AUTO-051`：第二批任务拆分文档 `SECOND_BATCH_AUTOMATION_TASKS.md` 已落地，执行顺序确认 `AUTO-052 -> AUTO-053 -> AUTO-054`。
- 后端单测已覆盖成绩导入关键边界：非 Excel、空数据、重复报名ID（`ScoreServiceImplTest`）。
- 已完成 `AUTO-052`、`AUTO-053`：导入细粒度运行时断言落地，核心 smoke 引入统一业务码断言 helper。
- `oneclick`、`role-paths`、`current-event workflow` 继续稳定通过，说明当前环境仍可稳定复跑。
- next_task: `AUTO-059` 赛事状态流转边界与撤回前置条件深测
