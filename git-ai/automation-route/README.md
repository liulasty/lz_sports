# Automation Route Working Agreement

`git-ai/automation-route` 是自动化迭代的唯一工作区，用于把“业务回归、缺陷修复、状态回写、下一轮任务”做成稳定闭环。

## 目录职责

- `BACKLOG.md`：任务状态真源（TODO/DOING/DONE/BLOCKED）。
- `BUSINESS_STATUS.md`：业务覆盖现状和缺陷入口真源。
- `FIRST_BATCH_AUTOMATION_TASKS.md`：当前批次业务链路清单与优先级。
- `WORKFLOW_OPEN_CLOSE_SMOKE.md`：开工到收工的执行循环规范。
- `plans/`：每轮计划。
- `runs/`：每轮执行记录。

## 执行边界

1. 自动化任务默认在 `D:\soft\lz_sports_git_ai`（`git-ai/automation-route` worktree）执行。
2. 不在主仓目录做整轮回归，避免端口、数据和产物互相污染。
3. 涉及业务代码修复时，先在本目录文档中明确任务边界，再改模块代码。

## 完整迭代最小循环

1. 读取 `PROJECT_LOOP.md`、`git-ai/automation-route/BACKLOG.md`、`git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md`、`git-ai/automation-route/BUSINESS_STATUS.md`、`git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md`。
2. 选择“最高优先级、未覆盖、无阻塞”的业务链路（当前默认 `AUTO-042`）。
3. 执行 `dev-start`，优先执行 `suite`，按需要补定向验证。
4. 失败先定位接口与根因，直接修复后重跑。
5. 同一问题连续失败 2 次：标记 `BLOCKED` 并写 `block_reason`。
6. 通过后回写 `runs + BUSINESS_STATUS + FIRST_BATCH + BACKLOG + next_task`。
7. 执行 `dev-stop` 收工。

## 分支同步标准 4 步（固定）

1. 在 `D:\soft\lz_sports_git_ai` 完成一轮任务并提交到 `git-ai/automation-route`。
2. 在 `D:\soft\lz_sports` 执行 `git checkout master && git merge git-ai/automation-route`。
3. 回到 `D:\soft\lz_sports_git_ai` 执行 `git merge master`，如有冲突按路径真源规则解决（例如 backlog 以 `git-ai/automation-route/BACKLOG.md` 为准）。
4. 任一目录执行 `git rev-parse master` 与 `git rev-parse git-ai/automation-route`，两个 hash 相同才算同步完成。

## AICoding 执行根目录

- `D:\soft\lz_sports_git_ai`：执行 AICoding 自动化迭代和整轮回归（默认目录）。
- `D:\soft\lz_sports`：做主线开发与分支合并，不执行整轮自动化回归。

## 不入库本地配置同步（方案 B 脚本）

当 `.cursor` 下存在不入库的本地文件（例如个人偏好、临时配置）时，使用脚本保持两个目录一致：

```powershell
# 主仓 -> worktree（开工前推荐）
powershell -ExecutionPolicy Bypass -File scripts/sync-local-cursor.ps1 -Direction toWorktree

# worktree -> 主仓（收工后如有本地变动）
powershell -ExecutionPolicy Bypass -File scripts/sync-local-cursor.ps1 -Direction toMain

# 仅预览，不实际写入
powershell -ExecutionPolicy Bypass -File scripts/sync-local-cursor.ps1 -Direction toWorktree -DryRun
```

## 懒人开工脚本（AICoding 前置检查）

一键执行分支、文档和本地配置同步检查：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/aicoding-precheck.ps1
```

或使用统一入口：

```bat
scripts\test.bat aicoding-precheck
```

```bash
./scripts/test.sh aicoding-precheck
```

## 发布脚本入口（统一到 scripts）

- `scripts/publish.ps1`
- `scripts/publish.bat`
- `scripts/publish.sh`

统一入口命令：

```bat
scripts\test.bat publish
```

```bash
./scripts/test.sh publish
```

## 结果汇报格式（固定）

- `changed_files`
- `key_changes`
- `test_results`
- `risks`
- `next_task`
