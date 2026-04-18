# PROJECT LOOP（LZ Sports, Machine First）

```yaml
project_loop:
  version: 1
  mode: iterative-agent
  architecture:
    repo_type: monorepo
    modules:
      - name: lz_sports_frontend
        stack: "Vue 3 + Vite"
        working_directory: "lz_sports_frontend"
      - name: lz_sports_backend
        stack: "Spring Boot 3 + Java 17"
        working_directory: "lz_sports_backend"
  sources:
    backlog: "git-ai/automation-route/BACKLOG.md"
  selection:
    strategy: "follow_backlog_selection_rule"
    fallback_when_no_executable_todo: "append_minimum_actionable_task_then_pick_first"
  execution:
    plan_steps: "3-5"
    implement_after_plan: true
    validate_after_change: true
  commands:
    frontend:
      lint: "npm run lint"
      test: "npm test"
      cwd: "lz_sports_frontend"
    backend:
      test: "mvn test"
      cwd: "lz_sports_backend"
  retries:
    auto_fix_before_report: true
    max_retries_before_blocked: 2
  backlog_maintenance:
    auto_append_minimum_task_when_empty: true
    minimum_task_template:
      priority: "P2"
      status: "TODO"
      dependencies: []
      block_reason: ""
  report:
    required_fields:
      - changed_files
      - key_changes
      - test_results
      - risks
      - next_task
```
