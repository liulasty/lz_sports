task_id: AUTO-038
goal: 将当前业务完整性回归扩展到报名拒绝分支，并验证拒绝后通知、状态查询及同项目重提仍可复跑。
scope_in_repo:
  - scripts/smoke-event-workflow.ps1
  - scripts/smoke-suite.ps1
  - git-ai/automation-route/runs/**
  - BACKLOG.md
out_of_scope:
  - 与报名拒绝链路无关的功能重构
  - 在 D:\soft\lz_sports 主仓库执行回归
validation_commands:
  - powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
  - powershell -ExecutionPolicy Bypass -File scripts/smoke-suite.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1 -AccountsFile git-ai/automation-route/accounts/business-accounts-latest.json -RunId 2026-04-18-auto-061 -RelatedTaskId AUTO-038
  - powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
rollback_notes: 如果拒绝分支在两轮修复后仍无法稳定复跑，则将 AUTO-038 标记为 BLOCKED，并在 backlog 中写明 block_reason、失败信号与解除条件。
