run_id: 2026-04-20-auto-045-score-manage
related_task_id: AUTO-045
changed_files:
  - scripts/smoke-score-manage.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-045-score-manage.md
key_changes:
  - Added a targeted score-manage smoke that verifies EVENT_ADMIN event visibility, score entry candidates, multi-row save, publish lock, and published query visibility on a newly created event.
  - The flow creates one assigned event and one unassigned control event so event/page and RequireEventAdmin boundaries can be asserted together.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      user_login: pass
      create_assigned_event: pass
      create_control_event: pass
      bind_event_admin: pass
      publish_events: pass
      event_scope: pass
      unauthorized_candidates: pass
      athlete_apply: pass
      athlete_approve: pass
      registration_apply: pass
      registration_approve: pass
      score_candidates_before_save: pass
      batch_save: pass
      score_candidates_after_save: pass
      score_publish: pass
      score_lock: pass
      score_queries: pass
artifacts:
  - assigned_event_id: "10"
  - assigned_event_name: "AUTO-045 Score Assigned 230300"
  - control_event_id: "11"
  - control_event_name: "AUTO-045 Score Control 230300"
  - registration_ids: "12,13"
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke after score-manage UI or score permission changes."
