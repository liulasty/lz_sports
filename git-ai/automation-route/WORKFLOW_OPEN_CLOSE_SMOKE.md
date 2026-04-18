# 开工/收工 + 整体业务回归循环说明

本文档用于 `git-ai/automation-route` 分支或对应 worktree 的本地迭代，目标是把“启动环境、整体验证、失败自修复、重跑、收工清理”固化成统一执行循环。

## 0) 分支前置条件

- 所有自动化迭代与业务回归默认在 `git-ai/automation-route` 分支执行。
- 如果主仓库当前不在该分支，优先进入对应 worktree，例如 `D:\soft\lz_sports_git_ai`。

## 1) 开工（启动本地开发环境）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
```

`dev-start` 默认会做：
- 读取 `config/.env.dev`
- 本地化适配（例如 `DB_USER -> DB_USERNAME`、`mysql/redis -> localhost`）
- 清理 `8080/5173` 端口占用
- 启动前端（5173）和后端（8080）
- 可选把后端启动输出落到 `git-ai/automation-route/runs/backend-dev.log`，便于后续从日志提取验证码

## 2) 整体业务回归（一键）

优先使用 `suite`，而不是只跑 `oneclick`。`suite` 会先执行基础基线回归，再继续执行当前赛事完整业务链路。

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-suite.ps1 -BackendUrl http://localhost:8080 -FrontendUrl http://localhost:5173 -EventId 1 -AccountsFile git-ai/automation-route/runs/business-accounts-latest.json
```

该脚本会自动执行：
- `oneclick`：分支检查、端口/健康检查、`full-smoke`、`role-paths`
- `event-workflow`：赛事详情、项目列表、运动员资格、普通用户负向报名、运动员报名、赛事管理员审核、通知增长、报名状态、成绩相关查询
- 账号资产失效时自动调用 `register-seed-users.ps1` 重建 `EVENT_ADMIN / USER / ATHLETE`
- 当前回归赛事状态或时间窗口不适配时，自动归一化目标赛事到可报名、可回放状态
- 自动写入 `git-ai/automation-route/runs/*.md` 运行记录

## 3) 统一执行循环

每轮迭代都按下面的闭环执行，直到“当前已有业务完整性”达到稳定状态：

1. 读取 `PROJECT_LOOP.md` 与 `BACKLOG.md`，选择最高优先级、无阻塞、依赖已完成的任务。
2. 如果 backlog 没有可执行 TODO，自动追加一条最小任务，默认围绕“当前已有业务完整性回归、缺陷修复、复跑验证”展开。
3. 开工后优先执行 `suite`；如果失败，先定位失败步骤和根因，再直接修复代码或脚本。
4. 修复后必须重跑对应最小验证；影响业务链路时，必须再次执行 `suite`。
5. 连续 2 次修复后仍失败，则将任务标记为 `BLOCKED`，写清 `block_reason`、当前信号和下一步解除条件。
6. 全部通过后补齐 `runs` 记录，并把下一条最小可执行任务追加回 backlog，继续下一轮。

## 4) 基础回归与完整性回归的关系

- `oneclick` 适合验证本地前后端是否启动正常，以及登录/报名/通知/角色路径基线是否还在。
- `suite` 适合验证“当前项目已有业务完整性”，因为它会在 `oneclick` 通过后继续回放当前赛事的核心业务链。
- 当修复只影响单个接口时，可以先跑定向验证；准备收敛结论前，仍要回到 `suite`。

## 5) 收工（停止本地服务）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
```

## 6) 建议的新对话提示词

```text
切到 git-ai/automation-route 分支（若主仓库不在该分支，则进入对应 worktree，例如 D:\soft\lz_sports_git_ai）。
读取 PROJECT_LOOP.md、BACKLOG.md 与 git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md，按统一执行循环开始迭代：
1) 若无可执行 TODO，则补一条最小可执行任务
2) 执行 dev-start
3) 优先执行 suite（oneclick + event-workflow）
4) 失败先自修复并重跑，连续 2 次失败则标记 BLOCKED 并写明 block_reason
5) 通过后补齐 runs 记录，更新 backlog 状态，并继续下一条任务
6) 结束时执行 dev-stop
```
