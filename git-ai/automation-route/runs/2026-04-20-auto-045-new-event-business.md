run_id: 2026-04-20-auto-045-new-event-business
related_task_id: AUTO-045
changed_files:
  - scripts/smoke-new-event-business.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-045-new-event-business.md
key_changes:
  - Added a targeted smoke that reuses existing SCHOOL_ADMIN, EVENT_ADMIN and USER accounts to complete the new event business chain.
  - The flow covers create event, bind event admin, publish OPEN, user athlete application, event-admin approval, project registration, and registration approval.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      user_login: pass
      create_event: pass
      bind_event_admin: pass
      publish_open: pass
      update_user_profile: pass
      athlete_apply: pass
      athlete_approve: pass
      athlete_status: pass
      registration_apply: pass
      registration_approve: pass
      registration_status: pass
artifacts:
  - event_id: "8"
  - event_name: "AUTO-045 New Event 230215"
  - project_id: "20"
  - project_name: "AUTO-045 Project 230215"
  - athlete_application_id: "3"
  - registration_id: "10"
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke to validate new-event lifecycle, athlete qualification, and registration approval together."
