# BACKLOG (Automation Route)

> version: 1
> status_enum: TODO | DOING | DONE | BLOCKED
> priority_enum: P0 | P1 | P2
> selection_rule: 按 priority 升序 + id 升序，选择第一个 status=TODO 且 dependencies 全部 DONE 且 block_reason 为空的任务

## tasks

```yaml
tasks:
  - id: AUTO-038
    title: 报名拒绝分支回归
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies: []
    block_reason: ""
    acceptance:
      - "覆盖 EVENT_ADMIN 拒绝报名后状态与通知变化"
      - "不破坏 suite 主链"

  - id: AUTO-039
    title: 用户禁用/启用与登录失败校验
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-038
    block_reason: ""
    acceptance:
      - "覆盖 disable -> 登录失败 -> enable -> 登录恢复"
      - "结果可稳定复跑"

  - id: AUTO-040
    title: 赛事管理最小闭环
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-039
    block_reason: ""
    acceptance:
      - "覆盖 DRAFT 创建、管理员绑定、OPEN 发布、撤回限制"
      - "清理逻辑可复用"

  - id: AUTO-041
    title: 成绩录入与发布最小闭环
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-040
    block_reason: ""
    acceptance:
      - "覆盖 upsert -> publish -> my/public 查询"
      - "已接入 suite current-event workflow"

  - id: AUTO-042
    title: 项目/部门接口权限边界与数据污染检查
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-041
    block_reason: ""
    acceptance:
      - "至少产出 1 条清晰的权限或污染风险验证结果"
      - "若发现缺陷，完成最小修复并回归验证"

  - id: AUTO-043
    title: 部门树可见性与组织模式联动回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-042
    block_reason: ""
    acceptance:
      - "覆盖 department/tree 在登录态与匿名态的可见性约束"
      - "覆盖 orgMode 切换后部门字段裁剪一致性（最小断言）"

  - id: AUTO-044
    title: 通知已读/全部已读与未读数一致性回归（含越权边界）
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-043
    block_reason: ""
    acceptance:
      - "覆盖 read(id) -> unread-count 变化 -> read-all -> unread-count=0 的最小闭环"
      - "覆盖跨用户 read 仍为 403（不放松权限）"
      - "不破坏 suite 主链"

  - id: AUTO-045
    title: 项目管理最小闭环与权限边界回归
    priority: P1
    status: TODO
    owner: agent
    area: automation
    dependencies:
      - AUTO-044
    block_reason: ""
    acceptance:
      - "覆盖 SCHOOL_ADMIN 或可用管理员创建/编辑/删除项目的最小闭环"
      - "覆盖非管理员角色对项目写接口仍被拒绝"
      - "不破坏 suite 主链"
```

## update_rules

- 更新状态时仅修改对应任务字段：`status`、`block_reason`、`dependencies`。
- 当任务进入 `BLOCKED`，必须填写 `block_reason`。
- 当任务从 `BLOCKED` 恢复，必须清空 `block_reason`。
- 新增任务时，`id` 必须全局唯一，推荐前缀：`AUTO`。

## next_task

- `AUTO-045` 项目管理最小闭环与权限边界回归

## latest_smoke

- `2026-04-19-auto-048` 已在 `git-ai/automation-route` worktree 通过。
- 本轮先发现本地环境处于 `init-status=false`，导致 `register-seed-users.ps1` 无法解析可用 `SCHOOL_ADMIN` 种子。
- 已按 `WORKFLOW_OPEN_CLOSE_SMOKE.md` 允许路径执行 `POST /api/system/init`，使用 `init_school_admin_01 / Admin12345` 恢复初始化态。
- 随后执行 `dev-stop -> dev-start -> smoke-suite`，基线与 current-event workflow 全部通过。
- `next_task` 保持为 `AUTO-045`，本轮未进入其实现阶段。
