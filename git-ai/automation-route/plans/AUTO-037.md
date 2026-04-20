task_id: AUTO-037
goal: 在 git-ai/automation-route worktree 执行一次完整的开工/业务回归/收工闭环，并沉淀本轮 runs 记录。
scope_in_repo:
  - scripts/dev-start.ps1
  - scripts/smoke-suite.ps1
  - scripts/dev-stop.ps1
  - git-ai/automation-route/runs/**
  - BACKLOG.md
out_of_scope:
  - 与本轮 suite 失败无关的功能性重构
  - 在 D:\soft\lz_sports 主仓库执行回归
validation_commands:
  - powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
  - powershell -ExecutionPolicy Bypass -File scripts/smoke-suite.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1 -AccountsFile git-ai/automation-route/accounts/business-accounts-latest.json -RunId 2026-04-18-auto-060 -RelatedTaskId AUTO-037
  - powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
rollback_notes: 如果 suite 连续两次失败且无法在本轮修复，则将 AUTO-037 标记为 BLOCKED，并在 backlog 中写清 block_reason 与解除条件。
