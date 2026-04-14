# PROJECT LOOP（LZ Sports, Machine First）

本文件用于让新对话快速进入“可执行迭代”状态；优先读取下方 YAML 配置块。

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
    api_proxy: "/api -> http://localhost:8080"
  sources:
    backlog: "BACKLOG.md"
    rules: ".cursor/rules/project-iteration-loop.mdc"
    trae_compat_mapping: "TRAE_CURSOR_MAPPING.md"
  selection:
    strategy: "follow_backlog_selection_rule"
    fallback_when_no_backlog: "create_minimum_backlog_then_pick_p0"
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
  constraints:
    forbidden_paths:
      - "/infra"
      - "/migrations"
    forbidden_secrets:
      - ".env"
      - "*.key"
      - "*.pem"
    keep_style_and_commit_convention: true
  report:
    required_fields:
      - changed_files
      - key_changes
      - test_results
      - risks
      - next_task
```

## 人类可读说明

1) 读取 `BACKLOG.md`，按其中 `selection_rule` 选择任务。  
2) 先给 3-5 步计划，再直接实施。  
3) 根据改动模块执行校验（前端 `lint/test`；后端 `mvn test`）。  
4) 失败先自修复，连续 2 次失败再上报阻塞。  
5) 输出固定字段：变更文件、关键改动、测试结果、风险、下一项建议。

## Trae 兼容说明

- 当对话中出现 `@.trae/documents`、`@.trae/skills`、`@.trae/specs` 引用时：
  - 若真实 `.trae` 存在，优先读取 `.trae`。
  - 若不存在，自动回退到 `TRAE_CURSOR_MAPPING.md` 的映射路径。

## 新对话启动口令（可直接粘贴）

```text
读取 PROJECT_LOOP.md 与 BACKLOG.md。
严格按 project_loop 配置执行：先选任务，给 3-5 步计划后直接实施。
修改后按模块运行校验命令；失败先自修复，连续 2 次失败再报告阻塞。
完成后按 required_fields 输出结果，并默认继续下一项。
```
