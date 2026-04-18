run_id: 2026-04-18-auto-064-user-status
related_task_id: AUTO-039
changed_files:
  - scripts/smoke-user-status.ps1
  - git-ai/automation-route/runs/2026-04-18-auto-064-user-status.md
key_changes:
  - Added a targeted user-status smoke that disables a seeded USER account, verifies login rejection, re-enables the user, and verifies login recovery.
  - Reused the current SCHOOL_ADMIN seed resolution and business-accounts asset file so the check can run without rebuilding fixture accounts.
test_results:
  - overall: fail
  - steps:
      school_admin_login: pass
      target_user_lookup: pass
      disable: pass
      disabled_login: pending
      enable: pending
      enabled_login: pending
risks:
  - "This smoke mutates the seeded USER account status and assumes the enable step always runs after disable."
  - "If the run aborts between disable and enable, the seeded USER account may remain disabled until manually restored."
issue_inputs:
  - source: local-run
    signal: "disabled-login failed: expected message about disabled account, got 'è´¦å·å·²ç¦ç¨ï¼è¯·èç³»ç®¡çå'"
repeated_failures:
  - issue_key: user-status-smoke
    attempts: 1
    blocked: false
next_task: "AUTO-040 鎵╁睍涓氬姟鍥炲綊鍒拌禌浜嬬鐞嗘渶灏忛棴鐜?
next_action: "Reuse this smoke after any admin-user status or auth-login change."
