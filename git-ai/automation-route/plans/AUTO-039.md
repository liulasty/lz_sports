task_id: AUTO-039
goal: 补齐 SCHOOL_ADMIN 禁用/启用普通用户后的登录失败与恢复登录回归验证，并保证不破坏现有账号资产主链。
scope_in_repo:
  - scripts/smoke-user-status.ps1
  - scripts/test.bat
  - scripts/test.sh
  - git-ai/automation-route/runs/**
  - git-ai/automation-route/BUSINESS_STATUS.md
  - git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md
  - BACKLOG.md
out_of_scope:
  - 用户角色变更与密码重置的完整覆盖
  - 在 D:\soft\lz_sports 主仓库执行回归
validation_commands:
  - powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
  - powershell -ExecutionPolicy Bypass -File scripts/smoke-suite.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1 -AccountsFile git-ai/automation-route/accounts/business-accounts-latest.json -RunId 2026-04-18-auto-063 -RelatedTaskId AUTO-039
  - powershell -ExecutionPolicy Bypass -File scripts/smoke-user-status.ps1 -BackendUrl http://localhost:8080 -AccountsFile git-ai/automation-route/accounts/business-accounts-latest.json -RunId 2026-04-18-auto-063-user-status -RelatedTaskId AUTO-039
  - powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
rollback_notes: 如果禁用/启用验证连续两次失败且无法通过脚本或代码修复，则将 AUTO-039 标记为 BLOCKED，并写明失败接口、返回信号与解除条件。
