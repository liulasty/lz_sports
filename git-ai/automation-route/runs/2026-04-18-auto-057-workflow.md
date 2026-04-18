run_id: 2026-04-18-auto-057-workflow
related_task_id: AUTO-035
changed_files:
  - scripts/smoke-event-workflow.ps1
  - git-ai/automation-route/runs/2026-04-18-auto-057-workflow.md
key_changes:
  - Added replayable current-event workflow smoke using the latest generated EVENT_ADMIN / USER / ATHLETE asset accounts.
  - The flow verifies event detail, project list, athlete qualification, user negative apply, athlete apply, event-admin approve, notification growth, registration status, and score-related queries.
test_results:
  - overall: fail
  - steps:
      event_detail: pass
      project_list: pass
      athlete_status: pass
      user_negative_apply: pass
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
    signal: "athlete-apply failed: expected [200], got code=409, msg=èµäºç¶æä¸æ¯ OPEN"
token_source:
  - "fresh /api/auth/login tokens from business-accounts-latest.json usernames/passwords"
repeated_failures:
  - issue_key: current-event-full-workflow
    attempts: 1
    blocked: false
next_action: "Reuse this workflow smoke after any event, registration, or notification change."
