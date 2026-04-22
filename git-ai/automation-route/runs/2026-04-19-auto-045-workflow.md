run_id: 2026-04-19-auto-045-workflow
related_task_id: AUTO-045
changed_files:
  - scripts/smoke-event-workflow.ps1
  - git-ai/automation-route/runs/2026-04-19-auto-045-workflow.md
key_changes:
  - Added replayable current-event workflow smoke using the latest generated EVENT_ADMIN / USER / ATHLETE asset accounts.
  - The flow verifies event detail, project list, athlete qualification, user negative apply, athlete apply, event-admin reject, rejection notification/status, same-project reapply, event-admin approve, approval notification, registration status, and score-related queries.
test_results:
  - overall: fail
  - steps:
      event_detail: pass
      project_list: pass
      athlete_status: pass
      user_negative_apply: pass
      athlete_apply: pass
      admin_reject: pass
      reject_notification: pass
      reject_registration_page: pass
      athlete_reapply: pass
      admin_audit: pass
      approve_notification: pass
      registration_page: pass
      score_upsert: pending
      score_publish: pending
      score_checks: pending
risks:
  - "Workflow replay mutates current local event data by creating one rejected registration, then re-applying and approving one registration."
  - "The flow depends on the freshness of business-accounts-latest.json and the current event being OPEN within the registration window."
issue_inputs:
  - source: local-run
    signal: "score-upsert failed: expected [200], got code=409, msg=å·²åå¸çæç»©ä¸å¯ä¿®æ¹"
token_source:
  - "fresh /api/auth/login tokens from business-accounts-latest.json usernames/passwords"
repeated_failures:
  - issue_key: current-event-full-workflow
    attempts: 1
    blocked: false
next_action: "Reuse this workflow smoke after any event, registration, or notification change."
