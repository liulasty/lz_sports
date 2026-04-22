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
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-044
    block_reason: ""
    acceptance:
      - "覆盖 SCHOOL_ADMIN 或可用管理员创建/编辑/删除项目的最小闭环"
      - "覆盖非管理员角色对项目写接口仍被拒绝"
      - "不破坏 suite 主链"

  - id: AUTO-046
    title: 环境与学校配置真实回放
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-045
    block_reason: ""
    acceptance:
      - "覆盖 school-config 更新、logo 上传、reset/init 状态切换与管理员登录恢复"
      - "reset/init 后可通过 suite 自愈回到可复跑基线"

  - id: AUTO-047
    title: 用户重置密码与列表筛选回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-046
    block_reason: ""
    acceptance:
      - "覆盖一次性重置 token、旧密码失效、新密码生效与密码回滚"
      - "覆盖 auth/list 筛选分页与 USER 越权访问拒绝"

  - id: AUTO-048
    title: 报名导出与权限边界回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-047
    block_reason: ""
    acceptance:
      - "覆盖 EVENT_ADMIN 导出成功、USER 导出拒绝"
      - "覆盖未绑定赛事的 EVENT_ADMIN 导出拒绝"

  - id: AUTO-049
    title: 成绩导入导出模板深测
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-048
    block_reason: ""
    acceptance:
      - "覆盖 score template/export/export-registration 导出可用性与权限边界"
      - "覆盖坏文件导入返回明确业务错误（code=400）"

  - id: AUTO-050
    title: 管理端统计口径回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-049
    block_reason: ""
    acceptance:
      - "覆盖 SCHOOL_ADMIN 的 overview/events 统计接口可达与返回结构"
      - "覆盖 totalRegistrations 与 events 聚合值一致性最小断言"

  - id: AUTO-051
    title: 第二批未覆盖链路拆分与优先级确认
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-050
    block_reason: ""
    acceptance:
      - "产出第二批任务清单并按 P0/P1 拆分优先级与依赖"
      - "回写 next_task 指向第二批首个可执行任务"

  - id: AUTO-052
    title: 成绩导入列级错误与重复行深测
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-051
    block_reason: ""
    acceptance:
      - "覆盖缺失关键列、空成绩、重复报名ID的导入断言"
      - "导入失败场景返回稳定业务码并产出 failure 明细"

  - id: AUTO-053
    title: 业务码断言统一化改造（HTTP 200 场景）
    priority: P0
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-052
    block_reason: ""
    acceptance:
      - "核心 smoke 脚本统一使用业务码断言工具函数"
      - "关键失败路径不再仅依赖 HTTP 状态码"

  - id: AUTO-054
    title: 赛事与项目管理完整 CRUD 回放
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-053
    block_reason: ""
    acceptance:
      - "覆盖 event/project 新增、编辑、删除、撤回与权限边界"
      - "不破坏 suite 主链"
  - id: AUTO-055
    title: 赛事/项目编辑接口字段约束与越权细测
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-054
    block_reason: ""
    acceptance:
      - "覆盖 event/project 编辑接口在最小必填字段与非法字段组合下的稳定业务返回"
      - "覆盖 SCHOOL_ADMIN/EVENT_ADMIN/USER 对编辑写接口的越权边界"

  - id: AUTO-056
    title: 赛事管理员跨赛事编辑越权回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-055
    block_reason: ""
    acceptance:
      - "覆盖 EVENT_ADMIN 编辑未绑定赛事应被拒绝（403）"
      - "覆盖 SCHOOL_ADMIN 绑定/解绑后 EVENT_ADMIN 权限变化可复跑"

  - id: AUTO-057
    title: 赛事管理员解绑后权限收敛回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-056
    block_reason: ""
    acceptance:
      - "覆盖 EVENT_ADMIN 在解绑前可编辑，解绑后编辑被拒绝（403）"
      - "不破坏 suite 主链"

  - id: AUTO-058
    title: 项目写接口角色边界回归
    priority: P1
    status: DONE
    owner: agent
    area: automation
    dependencies:
      - AUTO-057
    block_reason: ""
    acceptance:
      - "覆盖 SCHOOL_ADMIN 项目编辑通过，EVENT_ADMIN/USER 项目编辑拒绝（403）"
      - "不破坏 suite 主链"

  - id: AUTO-059
    title: 赛事状态流转边界与撤回前置条件深测
    priority: P1
    status: TODO
    owner: agent
    area: automation
    dependencies:
      - AUTO-058
    block_reason: ""
    acceptance:
      - "覆盖 OPEN->DRAFT 撤回前后的关键前置条件与业务码稳定性"
      - "覆盖 EVENT_ADMIN/SCHOOL_ADMIN 在状态流转接口的最小权限边界"
```

## update_rules

- 更新状态时仅修改对应任务字段：`status`、`block_reason`、`dependencies`。
- 当任务进入 `BLOCKED`，必须填写 `block_reason`。
- 当任务从 `BLOCKED` 恢复，必须清空 `block_reason`。
- 新增任务时，`id` 必须全局唯一，推荐前缀：`AUTO`。

## next_task

- `AUTO-059` 赛事状态流转边界与撤回前置条件深测

## latest_smoke

- `2026-04-20-auto-001`、`2026-04-20-auto-002` 基线 `smoke-suite` 均通过（含 reset/init 后自愈复跑），总览回写记录为 `2026-04-20-auto-003`。
- `2026-04-20-auto-006` 基线 `smoke-suite` 再次一次通过（`oneclick` + `current-event workflow` 全绿），当前环境可稳定复跑。
- `AUTO-054` 定向回归通过：`2026-04-20-auto-054-new-event-business`、`2026-04-20-auto-054-new-event-branches`、`2026-04-20-auto-054-event-admin` 全部通过；收敛 `smoke-suite` `2026-04-20-auto-007` 再次通过。
- `AUTO-055` 定向回归通过：`2026-04-20-auto-055-event-project-edit-boundary-rerun` 通过；修复脚本断言后按 `dev-stop -> dev-start -> suite(2026-04-20-auto-008)` 收敛通过。
- `AUTO-056~AUTO-058` 连续执行通过：`2026-04-20-auto-056-cross-event-edit`、`2026-04-20-auto-057-unbind-permission-shift`、`2026-04-20-auto-058-project-write-boundary` 全部通过；收敛 `smoke-suite` `2026-04-20-auto-009` 再次通过。
- `2026-04-20-auto-010` 基线 `smoke-suite` 再次通过（`oneclick` + `workflow` 全绿），作为当前迭代稳定性基线。
- `AUTO-045~AUTO-050` 本轮已完成：覆盖新赛事闭环、学校配置与 reset/init、重置密码与列表筛选、报名导出边界、成绩模板导出与坏文件导入、admin stats 口径校验。
- 新增专项记录：`2026-04-20-auto-049-template-export`，验证 `template/export/export-registration` 导出与 USER 越权拒绝。
- 已修复成绩导入边界：非 `xls/xlsx` 文件返回业务错误 `code=400`，并补充重复报名ID导入失败逻辑与单测。
- `AUTO-051` 已完成：第二批任务已拆分并确认优先级，next_task 切换为 `AUTO-052`。
- `AUTO-052` 已完成：运行时脚本新增“缺列头导入拒绝 + 混合成功/失败批次”断言，覆盖列级错误与重复行导入边界。
- `AUTO-053` 已完成：新增通用业务码断言 helper（`scripts/lib/business-code-assert.ps1`），并接入核心 smoke（`score-manage`、`score-template-export`）。
