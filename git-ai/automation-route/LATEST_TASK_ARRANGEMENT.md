# 最新任务编排（自动化主线）

更新时间：2026-04-20
来源：`BACKLOG.md`、`BUSINESS_STATUS.md`、`FIRST_BATCH_AUTOMATION_TASKS.md`、`SECOND_BATCH_AUTOMATION_TASKS.md`

## 1) 当前状态总览

- 已完成里程碑：
  - 第一批链路：`AUTO-038` ~ `AUTO-043`（DONE）
  - 第二批链路：`AUTO-052` ~ `AUTO-058`（DONE）
  - 中间补全：`AUTO-044` ~ `AUTO-051`（DONE）
- 当前唯一执行入口任务：
  - `AUTO-059`（TODO）

## 2) 执行优先级（最新）

按 `BACKLOG` 选择规则，当前应按以下顺序推进：

1. `AUTO-059` 赛事状态流转边界与撤回前置条件深测（TODO）
2. `AUTO-060` reset/init 后全链路稳定性收敛（建议新增）
3. `AUTO-061` 覆盖域收口与发布前核对（建议新增）

> 说明：`AUTO-060`、`AUTO-061` 为当前收口阶段建议任务，用于把“功能已覆盖”升级为“可宣告稳定”。

## 3) AUTO-059 执行安排（正在进入）

目标：

- 覆盖 `OPEN -> DRAFT` 撤回前置条件及业务码稳定性。
- 明确 `EVENT_ADMIN / SCHOOL_ADMIN` 在赛事状态流转接口上的权限边界。

最小回放矩阵：

- 正向：
  - `SCHOOL_ADMIN` 可发布、可在可撤回窗口撤回。
  - 已绑定 `EVENT_ADMIN` 可操作被授权赛事状态（按现实现）。
- 负向：
  - 未绑定 `EVENT_ADMIN` 状态流转应拒绝（403）。
  - 报名开始后撤回应拒绝（业务失败码，通常 409）。
  - 非管理员角色触发状态流转应拒绝（403）。

收敛门禁：

- 定向 smoke 通过；
- `smoke-suite` 回归通过；
- 运行记录回写到 `runs/` 并同步更新 `BACKLOG/BUSINESS_STATUS`。

## 4) 收口阶段安排（AUTO-060 / AUTO-061）

### AUTO-060（建议新增，P1）

主题：reset/init 后稳定性收敛验证

- 执行一次 reset/init（允许范围内）；
- 执行 `dev-stop -> dev-start -> smoke-suite`；
- 关键专项复核：school-config/logo、reset-password、auth/list、registration export、score template/import/export、admin stats。

通过标准：

- reset/init 后账号资产与主链可自愈；
- 关键专项抽样全部通过；
- 无连续失败问题进入 BLOCKED。

### AUTO-061（建议新增，P1）

主题：覆盖域收口与发布前核对

- 汇总“已覆盖能力矩阵”和“残余风险矩阵”；
- 连续 2~3 轮 suite 稳定性记录；
- 输出最终 `next_task`（若收口完成则切到新需求池）。

通过标准：

- 能给出“可发布/可继续迭代”的明确结论；
- 文档状态一致（BACKLOG、BUSINESS_STATUS、批次清单不冲突）。

## 5) 文档分工（防止再次混乱）

- `BACKLOG.md`：唯一任务状态真源（TODO/DOING/DONE/BLOCKED + next_task）。
- `BUSINESS_STATUS.md`：业务覆盖现状与结论。
- `FIRST_BATCH_AUTOMATION_TASKS.md`：历史批次归档（不再作为 next_task 依据）。
- `SECOND_BATCH_AUTOMATION_TASKS.md`：第二批执行轨迹与阶段小结。
- `LATEST_TASK_ARRANGEMENT.md`：当前执行看板（本文件）。

## 6) 过程产物泛化与裁剪

已执行裁剪：

- 从 `FIRST_BATCH_AUTOMATION_TASKS.md` 移除逐轮“最新回写”明细，保留归档摘要与入口指针。
- 细节统一沉淀到 `runs/*.md`，避免同一信息在多文件重复。

后续统一模板：

- 使用 `PROCESS_ARTIFACT_TEMPLATE.md` 作为回写格式基线。
- 规则：状态写 `BACKLOG`、结论写 `BUSINESS_STATUS`、明细写 `runs`、批次文档只保留定义与衔接。
