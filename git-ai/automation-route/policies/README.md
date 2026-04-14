# Policies Template

Use this folder to keep route-level policies for automation execution.

## Baseline Policy

1. Route-first: automation artifacts default to `git-ai/automation-route`.
2. Single concern: each run should map to one acceptance target.
3. Gate alignment:
   - Frontend changes: `npm run lint` and `npm test`
   - Backend changes: `mvn -Pnon-container-baseline test`
4. Retry rule: auto-fix first; if same issue fails twice, mark BLOCKED with reason.
5. Run-record discipline: every run file must include issue source, root cause, and fix traces.

## Change Control

- Do not include secrets (`.env`, `*.key`, `*.pem`).
- Do not write automation orchestration files outside this route unless user-approved.
- For issue tracking, use stable `issue_key` naming to support repeated-failure counting.
