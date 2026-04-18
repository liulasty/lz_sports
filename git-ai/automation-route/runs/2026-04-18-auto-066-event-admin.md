run_id: 2026-04-18-auto-066-event-admin
related_task_id: AUTO-040
changed_files:
  - scripts/smoke-event-admin.ps1
  - git-ai/automation-route/runs/2026-04-18-auto-066-event-admin.md
key_changes:
  - Added a targeted event-admin smoke that creates a DRAFT event with one project, binds a seeded EVENT_ADMIN, publishes to OPEN, checks athlete visibility, verifies withdraw rejection after registration starts, then performs cleanup.
  - The smoke reuses the latest business role asset file and SCHOOL_ADMIN seed resolution so the lifecycle can be replayed without rebuilding the environment.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      create_event: pass
      athlete_draft_hidden: pass
      bind_admin: pass
      admin_list: pass
      publish_open: pass
      athlete_open_visible: pass
      withdraw_rejected: pass
      withdraw_cleanup: pass
      delete_cleanup: pass
issue_inputs:
  - source: local-run
    signal: "none"
artifacts:
  - created_event_id: "3"
  - created_event_name: "AUTO-040 Event Lifecycle 212550"
repeated_failures:
  - issue_key: event-admin-smoke
    attempts: 1
    blocked: false
next_task: "AUTO-041 鎵╁睍涓氬姟鍥炲綊鍒版垚缁╁綍鍏ヤ笌鍙戝竷鏈€灏忛棴鐜?
next_action: "Reuse this smoke after any event or event-admin permission/status change."
