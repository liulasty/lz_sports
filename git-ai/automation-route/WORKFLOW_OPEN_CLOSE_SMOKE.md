# 开工/收工 + 业务回归使用说明（完整迭代闭环）

本文档用于日常本地迭代，目标是把“启动环境、业务回归、失败修复、状态回写、收工清理”统一成可复用步骤。

## 0) 工作区与分支前置条件（双目录协同）

- 本项目推荐双目录协同：
  - 主仓库：`D:\soft\lz_sports`（通常用于 `master` 日常开发）
  - 自动化 worktree：`D:\soft\lz_sports_git_ai`（用于 `git-ai/automation-route` 自动化回归）
- **整轮回归只在** `D:\soft\lz_sports_git_ai` 执行，避免与主仓库日常改动互相干扰。
- 进目录后先执行 `git branch --show-current`，确认当前分支符合预期。
- `git stash` 为仓库级共享（在两个目录都可见）；使用时务必加清晰 message，避免误用。
- 若存在未提交噪音，优先用可恢复方式清理后再开工：

```powershell
# worktree
git stash push -u -m "auto-clean-<date>-worktree"

# main repo
git stash push -u -m "auto-clean-<date>-main"
```

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

## 2) 业务回归（优先 suite）

优先使用 `suite`（oneclick + event-workflow），而不是仅跑 `oneclick`。

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-suite.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1 -AccountsFile git-ai/automation-route/runs/business-accounts-latest.json
```

`suite` 会自动执行：
- `oneclick`：分支检查、端口/健康检查、`full-smoke`、`role-paths`
- `event-workflow`：当前赛事业务链路回放（报名申请/审核、通知增长、成绩录入/发布等）
- 自动写入 `git-ai/automation-route/runs/*.md` 运行记录

当只需快速验证基线时，再单独执行 `oneclick`：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-oneclick.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1
```

该脚本会自动执行：
- 分支检查
- 端口/健康检查
- full-smoke（登录/报名/成绩 + 通知）
- role-paths（SUPER_ADMIN / SCHOOL_ADMIN / EVENT_ADMIN / USER / ATHLETE）
- 自动写入 `git-ai/automation-route/runs/*.md` 运行记录

## 3) 统一执行循环（保证完整闭环）

每轮迭代按以下步骤执行，不跳步：

1. 先读取：`PROJECT_LOOP.md`、`git-ai/automation-route/BACKLOG.md`、`git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md`、`git-ai/automation-route/BUSINESS_STATUS.md`、`git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md`。
2. 任务选择以业务链路为主：优先“最高优先级、未覆盖、无阻塞”项。
3. 执行 `dev-start`，然后优先执行 `suite` 作为基线。
4. 若失败：定位到具体接口与根因，直接修复代码/脚本后重跑；先最小定向验证，再回归 `suite`。
5. 同一问题连续 2 次失败：标记 `BLOCKED` 并填写 `block_reason`（失败信号、根因假设、解除条件）。
6. 通过后必须回写：`runs`、`BUSINESS_STATUS.md`、`FIRST_BATCH_AUTOMATION_TASKS.md`、`BACKLOG.md`（含 `next_task`）。
7. 执行 `dev-stop` 收工。

## 4) 收工（停止本地服务）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
```

常用参数：

```powershell
# 仅查看会停止哪些进程
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1 -DryRun
```

## 5) 统一入口（test 脚本）

Windows:

```bat
scripts\test.bat dev-start
scripts\test.bat suite http://localhost:8080 http://localhost:5173 1 git-ai/automation-route/runs/business-accounts-latest.json
scripts\test.bat oneclick http://localhost:8080 http://localhost:5173 1
scripts\test.bat dev-stop
```

Linux/macOS:

```bash
./scripts/test.sh dev-start
./scripts/test.sh suite http://localhost:8080 http://localhost:5173 1 git-ai/automation-route/runs/business-accounts-latest.json
./scripts/test.sh oneclick http://localhost:8080 http://localhost:5173 1
./scripts/test.sh dev-stop
```

## 6) 新对话可复用提示词

以下提示词可直接复制到新对话使用。

### 开工提示词

```text
在 D:\soft\lz_sports_git_ai 这个 git-ai/automation-route worktree 开工（不要在 D:\soft\lz_sports 主仓库跑整轮回归）。
执行开工预检并启动：
1) 读取 config/.env.dev 并做本地化适配（DB_USER->DB_USERNAME、mysql/redis->localhost）
2) 清理 8080/5173 端口占用
3) 启动前后端（8080/5173）
4) 回报启动结果和端口健康状态
```

### 业务测试提示词（统一执行循环版）

```text
读取 PROJECT_LOOP.md、git-ai/automation-route/BACKLOG.md、git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md、git-ai/automation-route/BUSINESS_STATUS.md、git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md，按“统一执行循环”开始迭代：
先选最高优先级无阻塞项，给出 3-5 步计划后直接实施。
优先执行业务回归：suite（oneclick + event-workflow），必要时再补对应业务域定向验证。
失败先自修复，连续 2 次失败标记 BLOCKED 并写明 block_reason。
完成后更新 runs 记录、BUSINESS_STATUS/FIRST_BATCH_AUTOMATION_TASKS 覆盖现状、BACKLOG 状态与 next_task，并按 changed_files/key_changes/test_results/risks/next_task 汇报。
```

### 收工提示词

```text
执行收工流程：
1) 按端口停止本地前后端（默认 8080/5173）
2) 输出停止结果（PID/端口）
3) 如有本轮迭代，补齐 runs 记录并给出下一轮建议
```

