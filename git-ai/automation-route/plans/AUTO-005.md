task_id: AUTO-005
goal: Script key frontend-backend smoke checks for login, registration, and score paths.
scope_in_repo:
  - scripts/smoke-linkup.ps1
  - scripts/smoke-linkup.bat
  - BACKLOG.md
out_of_scope:
  - infra/**
  - migrations/**
validation_commands:
  - powershell -ExecutionPolicy Bypass -File scripts/smoke-linkup.ps1 -DryRun
rollback_notes: Revert smoke script files if path contracts need redesign.

execution_steps:
  - Implement reusable smoke script with DryRun support.
  - Cover checks: frontend availability, backend init status, login, registration query, score query.
  - Add automation-route run record and update backlog task state.
