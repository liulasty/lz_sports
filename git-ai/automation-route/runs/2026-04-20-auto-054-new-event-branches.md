run_id: 2026-04-20-auto-054-new-event-branches
related_task_id: AUTO-054
changed_files:
  - scripts/smoke-new-event-branches.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-054-new-event-branches.md
key_changes:
  - Added a targeted smoke that covers reject-athlete, reapply-athlete, reject-registration, reapply-registration, and score publish branches on a newly created event.
  - The flow reuses existing SCHOOL_ADMIN, EVENT_ADMIN, and USER accounts and keeps all assertions inside the same event lifecycle.
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
      athlete_apply_first: pass
      athlete_reject: pass
      athlete_rejected_status: pass
      athlete_reapply: pass
      athlete_approve: pass
      athlete_approved_status: pass
      registration_apply_first: pass
      registration_reject: pass
      registration_rejected_status: pass
      registration_reapply: pass
      registration_approve: pass
      registration_approved_status: pass
      score_upsert: pass
      score_publish: pass
      score_checks: pass
artifacts:
  - event_id: "13"
  - event_name: "AUTO-046 Branch Event 233504"
  - project_id: "26"
  - project_name: "AUTO-046 Branch Project 233504"
  - first_application_id: "9"
  - second_application_id: "10"
  - first_registration_id: "19"
  - second_registration_id: "19"
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke after athlete audit, registration audit, or score workflow changes."
