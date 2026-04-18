task_id: AUTO-035
goal: 建立“开工 -> 整体业务回归 -> 失败自修复 -> 重跑 -> 收工”的统一执行循环，并用真实 suite 回归验证当前已有业务完整性。
scope_in_repo:
  - git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md
  - git-ai/automation-route/plans/AUTO-035.md
  - git-ai/automation-route/runs/**
  - scripts/dev-start.ps1
  - scripts/smoke-oneclick.ps1
  - scripts/smoke-suite.ps1
  - scripts/smoke-event-workflow.ps1
  - scripts/test.bat
  - scripts/test.sh
out_of_scope:
  - 新增超出当前赛事/报名/通知/成绩已有业务之外的新功能
  - 非本地联调环境的部署改造
validation_commands:
  - powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
  - scripts/test.bat suite http://localhost:8080 http://localhost:5173 1 git-ai/automation-route/runs/business-accounts-latest.json
  - powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
rollback_notes: 若新增循环文档或脚本入口引发误导，回退本任务新增文档与对应入口说明，保留已验证可用的单次 smoke 能力。
