run_id: 2026-04-20-auto-049-template-export
related_task_id: AUTO-049
changed_files:
  - scripts/smoke-score-template-export.ps1
  - git-ai/automation-route/runs/2026-04-20-auto-049-template-export.md
key_changes:
  - Added dedicated score template/export smoke that validates template export, score export, registration export, and role boundary.
  - Added bad-file import negative assertion to ensure invalid file types are rejected with business code 400.
test_results:
  - overall: pass
  - steps:
      school_admin_login: pass
      event_admin_login: pass
      user_login: pass
      athlete_login: pass
      mixed_candidate_prepare: pass
      template_export_event_admin: pass
      score_export_event_admin: pass
      registration_export_event_admin: pass
      template_export_user_forbidden: pass
      bad_import_rejected: pass
      missing_header_import_rejected: pass
      mixed_valid_invalid_import: pass
issue_inputs:
  - source: local-run
    signal: "none"
next_action: "Reuse this smoke after score import/export/template changes."
