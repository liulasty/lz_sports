run_id: 2026-04-20-auto-034-forgot-once
related_task_id: AUTO-034
changed_files:
  - scripts/smoke-forgot-once.ps1
key_changes:
  - Added dedicated repeatable smoke for forgot-password token single-use chain.
test_results:
  - overall: pass
  - steps:
      backend_health: pass
      send_code: pass
      verify_code: pass
      reset_once: pass
      reset_reuse_block: pass
      login_old_blocked: pass
      login_new_ok: pass
      restore_password: pass
  - inputs:
      username: "ops_user_012722"
      email: "user012722@qq.com"
risks:
  - "Depends on backend log accessibility for extracting verification code in local runs."
issue_inputs:
  - source: local-run
    signal: "none"
token_source:
  - "reset token from /api/auth/verify-code"
repeated_failures:
  - issue_key: forgot-password-token-once
    attempts: 1
    blocked: false
next_action: "Run scripts/test.(bat|sh) forgot-once for repeat validation."
