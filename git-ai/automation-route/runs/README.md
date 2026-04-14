# Runs Template

Use this folder to record each automation execution result.

## Required Fields

- run_id
- related_task_id
- changed_files
- key_changes
- test_results
- risks
- issue_inputs
- root_causes
- fixes_applied
- regression_watch
- repeated_failures
- next_action

## Minimal Example

```md
run_id: 2026-04-14-auto-002
related_task_id: AUTO-002
changed_files:
  - git-ai/automation-route/plans/README.md
key_changes:
  - Added planning template fields
test_results:
  - ReadLints: clean
risks:
  - No runtime validation command executed for docs-only change
issue_inputs:
  - source: ci-log
    signal: lint warning count increased
root_causes:
  - Missing eslint config entry for new folder
fixes_applied:
  - Added lint include for target folder
regression_watch:
  - verify next run keeps warning count stable
repeated_failures:
  - issue_key: lint-warning-increase
    attempts: 1
    blocked: false
next_action: Start BE-012 plan entry in plans/
```

## Auto-Capture Rule

- Every new run record must include `issue_inputs/root_causes/fixes_applied/repeated_failures`.
- If no issue is found, explicitly write `none` to keep records queryable.
