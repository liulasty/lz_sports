# 开工/收工 + 业务回归使用说明

本文档用于日常本地迭代，目标是把“启动环境、业务回归、收工清理”统一成可复用步骤。

## 0) 分支前置条件

- 所有自动化迭代与回归脚本默认在 `git-ai/automation-route` 分支执行。
- 若当前不在该分支，请先切换（或使用对应 worktree）。

## 1) 开工（启动本地开发环境）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1
```

`dev-start` 默认会做：
- 读取 `config/.env.dev`
- 本地化适配（例如 `DB_USER -> DB_USERNAME`、`mysql/redis -> localhost`）
- 清理 8080/5173 端口占用
- 启动前端（5173）和后端（8080）

常用参数：

```powershell
# 只验证步骤，不实际启动
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -DryRun

# 仅启动后端（跳过前端）
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -SkipFrontend
```

## 2) 业务回归（一键）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-oneclick.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1
```

该脚本会自动执行：
- 分支检查
- 端口/健康检查
- full-smoke（登录/报名/成绩 + 通知）
- role-paths（SUPER_ADMIN / SCHOOL_ADMIN / EVENT_ADMIN / USER / ATHLETE）
- 自动写入 `git-ai/automation-route/runs/*.md` 运行记录

## 3) 收工（停止本地服务）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
```

常用参数：

```powershell
# 仅查看会停止哪些进程
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1 -DryRun
```

## 4) 统一入口（test 脚本）

Windows:

```bat
scripts\test.bat dev-start
scripts\test.bat oneclick http://localhost:8080 http://localhost:5173 1
scripts\test.bat dev-stop
```

Linux/macOS:

```bash
./scripts/test.sh dev-start
./scripts/test.sh oneclick http://localhost:8080 http://localhost:5173 1
./scripts/test.sh dev-stop
```

## 5) 新对话可复用提示词

以下提示词可直接复制到新对话使用。

### 开工提示词

```text
切到 git-ai/automation-route 分支（若被 worktree 占用则进入对应 worktree）。
执行开工预检并启动：
1) 读取 config/.env.dev 并做本地化适配（DB_USER->DB_USERNAME、mysql/redis->localhost）
2) 清理 8080/5173 端口占用
3) 启动前后端（8080/5173）
4) 回报启动结果和端口健康状态
```

### 业务测试提示词（统一执行循环版）

```text
读取 PROJECT_LOOP.md 与 BACKLOG.md，按“统一执行循环”开始迭代：
先选最高优先级无阻塞项，给出 3-5 步计划后直接实施。
优先执行业务回归：oneclick（分支检查 + 端口检查 + full-smoke + rolepaths）。
失败先自修复，连续 2 次失败标记 BLOCKED 并写明 block_reason。
完成后更新 git-ai/automation-route/runs 新记录，并按 changed_files/key_changes/test_results/risks/next_task 汇报。
```

### 收工提示词

```text
执行收工流程：
1) 按端口停止本地前后端（默认 8080/5173）
2) 输出停止结果（PID/端口）
3) 如有本轮迭代，补齐 runs 记录并给出下一轮建议
```

