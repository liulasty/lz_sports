# 第二批未覆盖业务链路自动化任务清单（执行中）

> 使用说明：本清单记录第二批任务轨迹；当前“最新安排”以 `BACKLOG.md` 的 `next_task` 为准。  
> 为避免多文档漂移，日常执行请先看：`git-ai/automation-route/LATEST_TASK_ARRANGEMENT.md`。

本文档承接 `AUTO-051`，用于把第二批未覆盖链路拆分为可执行任务，并给出优先级与依赖顺序。

## 目标

- 收敛“HTTP 200 + 业务 code”场景的断言一致性，减少误判。
- 加深成绩导入的列级错误/重复行断言，覆盖更真实的数据脏输入。
- 继续扩展赛事与项目管理端的完整 CRUD 回放，但不破坏 `smoke-suite` 主链。

## 任务分解

### AUTO-052（P0）成绩导入列级错误与重复行深测

- 范围：
  - 非法文件类型、空数据、缺失关键列、空成绩、重复报名ID
  - 校验 `successCount/failCount/failures` 与业务码稳定性
- 依赖：`AUTO-051`
- 通过标准：
  - 至少 3 类列级错误能稳定复现并断言
  - 重复报名ID被识别为失败行并有明确 failure 消息
- 当前状态：DONE（已覆盖坏文件、缺列头、重复报名ID、混合成功/失败批次）

### AUTO-053（P0）业务码断言统一化改造（HTTP 200 场景）

- 范围：
  - `registration/score/auth/admin-stats` 相关 smoke 的失败断言
  - 统一 helper：优先断言业务 `code`，再辅助检查 HTTP
- 依赖：`AUTO-052`
- 通过标准：
  - 核心 smoke 脚本不再仅凭 HTTP 200 判定通过
  - 关键失败路径（403/409/400）均有业务码断言
- 当前状态：DONE（新增 `scripts/lib/business-code-assert.ps1`，并接入 `smoke-score-manage.ps1`、`smoke-score-template-export.ps1`）

### AUTO-054（P1）赛事与项目管理完整 CRUD 回放

- 范围：
  - event/project 的 create/update/delete/withdraw 全链路
  - SCHOOL_ADMIN、EVENT_ADMIN、USER 三角色边界
- 依赖：`AUTO-053`
- 通过标准：
  - CRUD 正向闭环可复跑
  - 越权路径稳定拒绝且不破坏 `suite`
- 当前状态：DONE（`smoke-new-event-business`、`smoke-new-event-branches`、`smoke-event-admin` 定向通过，且 `smoke-suite` 收敛通过）

### AUTO-055（P1）赛事/项目编辑接口字段约束与越权细测

- 范围：
  - `event/project` 编辑接口的最小字段更新与非法时间窗口校验
  - `SCHOOL_ADMIN/EVENT_ADMIN/USER` 写接口权限边界
- 依赖：`AUTO-054`
- 通过标准：
  - SCHOOL_ADMIN 与已绑定 EVENT_ADMIN 的编辑路径稳定通过
  - USER 对 event/project 编辑写接口稳定拒绝（403）
- 当前状态：DONE（`smoke-event-project-edit-boundary` 通过，且 `smoke-suite` 收敛通过）

### AUTO-056（P1）赛事管理员跨赛事编辑越权回归

- 范围：
  - EVENT_ADMIN 编辑已绑定赛事与未绑定赛事的权限差异
- 依赖：`AUTO-055`
- 通过标准：
  - 已绑定赛事编辑可通过
  - 未绑定赛事编辑被拒绝（403）
- 当前状态：DONE（`smoke-event-admin-permission-shift` 场景 `cross-event-edit` 通过）

### AUTO-057（P1）赛事管理员解绑后权限收敛回归

- 范围：
  - EVENT_ADMIN 在解绑前后的赛事编辑权限变化
- 依赖：`AUTO-056`
- 通过标准：
  - 解绑前编辑通过
  - 解绑后编辑被拒绝（403）
- 当前状态：DONE（`smoke-event-admin-permission-shift` 场景 `unbind-permission-shift` 通过）

### AUTO-058（P1）项目写接口角色边界回归

- 范围：
  - 项目编辑写接口的角色边界：SCHOOL_ADMIN/EVENT_ADMIN/USER
- 依赖：`AUTO-057`
- 通过标准：
  - SCHOOL_ADMIN 编辑通过
  - EVENT_ADMIN 与 USER 编辑拒绝（403）
- 当前状态：DONE（`smoke-event-admin-permission-shift` 场景 `project-write-boundary` 通过）

## 执行顺序（确认）

1. `AUTO-052`（P0）- DONE
2. `AUTO-053`（P0）- DONE
3. `AUTO-054`（P1）- DONE
4. `AUTO-055`（P1）- DONE
5. `AUTO-056`（P1）- DONE
6. `AUTO-057`（P1）- DONE
7. `AUTO-058`（P1）- DONE

## 当前衔接

- `AUTO-059` 已在 `BACKLOG.md` 设为当前 `next_task`（TODO）。
- 第二批后续收口建议任务（`AUTO-060`、`AUTO-061`）见 `LATEST_TASK_ARRANGEMENT.md`。

## 回写约束

- 每轮结束必须同步：
  - `git-ai/automation-route/runs/*.md`
  - `git-ai/automation-route/BACKLOG.md`
  - `git-ai/automation-route/BUSINESS_STATUS.md`
- 新增脚本必须提供 `scripts/test.bat` 对应入口，便于一键复跑。
