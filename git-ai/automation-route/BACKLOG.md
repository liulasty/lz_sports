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
    status: TODO
    owner: agent
    area: automation
    dependencies:
      - AUTO-041
    block_reason: ""
    acceptance:
      - "至少产出 1 条清晰的权限或污染风险验证结果"
      - "若发现缺陷，完成最小修复并回归验证"
```

## update_rules

- 更新状态时仅修改对应任务字段：`status`、`block_reason`、`dependencies`。
- 当任务进入 `BLOCKED`，必须填写 `block_reason`。
- 当任务从 `BLOCKED` 恢复，必须清空 `block_reason`。
- 新增任务时，`id` 必须全局唯一，推荐前缀：`AUTO`。
