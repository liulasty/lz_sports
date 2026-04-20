run_id: 2026-04-20-auto-055-event-project-edit-boundary
related_task_id: AUTO-055
changed_files:
  - scripts/smoke-event-project-edit-boundary.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-055-event-project-edit-boundary.md
key_changes:
  - Added event/project edit boundary smoke for SCHOOL_ADMIN, EVENT_ADMIN and USER roles.
  - Verified valid event/project updates, invalid project time rejection, and USER write-path forbidden checks.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      user_login: pass
      create_event: pass
      bind_event_admin: pass
      publish_open: pass
      school_admin_event_update: pass
      user_event_update_forbidden: pass
      event_admin_event_update: pass
      school_admin_project_update: pass
      school_admin_project_invalid_time: pass
      user_project_update_forbidden: pass
      cleanup_event_delete: pass
artifacts:
  - event_id: "17"
  - project_id: "30"
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke when event/project update permissions or validation rules change."
