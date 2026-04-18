run_id: 2026-04-18-auto-054-workflow
related_task_id: AUTO-035
changed_files:
  - scripts/smoke-event-workflow.ps1
  - git-ai/automation-route/runs/2026-04-18-auto-054-workflow.md
key_changes:
  - Added replayable current-event workflow smoke using the latest generated EVENT_ADMIN / USER / ATHLETE asset accounts.
  - The flow verifies event detail, project list, athlete qualification, user negative apply, athlete apply, event-admin approve, notification growth, registration status, and score-related queries.
test_results:
  - overall: fail
  - steps:
      event_detail: pending
      project_list: pending
      athlete_status: pending
      user_negative_apply: pending
      athlete_apply: pending
      admin_audit: pending
      notification: pending
      registration_page: pending
      score_checks: pending
risks:
  - "Workflow replay mutates current local event data by creating and approving one registration."
  - "The flow depends on the freshness of business-accounts-latest.json and the current event being OPEN within the registration window."
issue_inputs:
  - source: local-run
    signal: "event-detail failed: expected [200], got code=404, msg=èµæºä¸å­å¨"
token_source:
  - "fresh /api/auth/login tokens from business-accounts-latest.json usernames/passwords"
repeated_failures:
  - issue_key: current-event-full-workflow
    attempts: 1
    blocked: false
next_action: "Reuse this workflow smoke after any event, registration, or notification change."
