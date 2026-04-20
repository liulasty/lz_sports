run_id: 2026-04-20-auto-058-project-write-boundary
related_task_id: AUTO-058
scenario: project-write-boundary
changed_files:
  - scripts/smoke-event-admin-permission-shift.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-058-project-write-boundary.md
key_changes:
  - Added reusable permission smoke for EVENT_ADMIN cross-event edit, unbind permission shift, and project write boundary checks.
  - Reused SCHOOL_ADMIN/EVENT_ADMIN/USER business assets and included cleanup for temporary events.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      user_login: pass
      scenario_main: pass
      cleanup: pass
artifacts:
  - event_ids: "21"
  - project_ids: "34"
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke after event-admin permission, bind/unbind, or event/project write-access changes."
