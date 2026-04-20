# Runs Template

Use this folder to record each automation execution result.

## Required Fields

- run_id
- related_task_id
- changed_files
- key_changes
- test_results
- risks
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
next_action: Start BE-012 plan entry in plans/
```
