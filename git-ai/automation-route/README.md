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

## 结果汇报格式（固定）

- `changed_files`
- `key_changes`
- `test_results`
- `risks`
- `next_task`
