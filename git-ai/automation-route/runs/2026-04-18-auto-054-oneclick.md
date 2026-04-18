run_id: 2026-04-18-auto-054-oneclick
related_task_id: AUTO-035
changed_files:
  - scripts/smoke-oneclick.ps1
key_changes:
  - Added one-click regression entrypoint (branch guard + endpoint checks + full-smoke + rolepaths) with auto run record output
test_results:
  - overall: pass
  - steps:
      branch_guard: pass
      availability: pass
      full_smoke: pass
      role_paths: pass
  - endpoints:
      frontend: "http://localhost:5173"
      backend: "http://localhost:8080"
      eventId: 1
risks:
  - "One-click relies on existing local services and fixture accounts; backend env/DB/Redis must be ready."
issue_inputs:
  - source: local-run
    signal: "none"
root_causes:
  - none
fixes_applied:
  - none
token_source:
  - "tokens from /api/auth/login inside smoke scripts"
regression_watch:
  - "If fixture accounts change, update scripts/smoke-rolepaths.ps1 defaults."
repeated_failures:
  - issue_key: oneclick-regression
    attempts: 1
    blocked: false
next_action: "Run scripts/test.(bat|sh) oneclick to ensure the wrapper is stable."
