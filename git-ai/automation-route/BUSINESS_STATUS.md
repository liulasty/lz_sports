# 业务状态（瘦身版）

> 本文件已瘦身：只保留“结论层 + 入口索引”。  
> 历史长文归档：`git-ai/automation-route/history/2026-04-20/BUSINESS_STATUS.full.md`。

## 当前结论（滚动）

- **回归入口**：`scripts/smoke-suite.ps1`（oneclick + event-workflow）
- **账号资产入口**：`git-ai/automation-route/accounts/business-accounts-latest.json`
- **明细证据**：`git-ai/automation-route/runs/*.md`
- **状态真源**：`git-ai/automation-route/BACKLOG.md`
- **当前任务**：`AUTO-059`（见 `BACKLOG.md` 的 `next_task`）
- **最新基线**：`2026-04-20-auto-010` `smoke-suite` 通过（含 `oneclick` + `workflow`）。

## 覆盖域（已收敛通过的关键能力）

- init/reset + school-config/logo：已回放通过（详见 `runs` 与历史快照）
- reset-password + 用户列表筛选：已回放通过
- 报名导出/边界：已回放通过
- 成绩导入/导出模板：已回放通过
- admin stats：已回放通过

## 变更规则（避免漂移）

- 每轮只在此处更新 **最近 1~3 条结论**（不要复制 runs 明细）。
- 任务状态与 `next_task` 只写 `BACKLOG.md`。
