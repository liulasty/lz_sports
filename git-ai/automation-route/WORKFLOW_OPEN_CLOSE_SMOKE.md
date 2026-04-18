# 开工/收工 + 整体业务回归循环说明

本文档用于 `git-ai/automation-route` 分支或对应 worktree 的本地迭代，目标是把“启动环境、整体验证、失败自修复、重跑、收工清理”固化成统一执行循环。

## 0) 分支前置条件

- 所有自动化迭代与业务回归默认在 `git-ai/automation-route` 分支执行。
- 如果主仓库当前不在该分支，优先进入对应 worktree，例如 `D:\soft\lz_sports_git_ai`。

## 1) 开工（启动本地开发环境）

开工前先同步本地 `.cursor`（不入库内容）到 worktree，避免提示词和本地配置漂移：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sync-local-cursor.ps1 -Direction toWorktree
```

然后执行 AICoding 前置检查（分支/文档/本地同步）：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/aicoding-precheck.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1 -BackendLogPath git-ai/automation-route/runs/backend-dev.log
```

`dev-start` 默认会做：
- 读取 `config/.env.dev`
- 本地化适配（例如 `DB_USER -> DB_USERNAME`、`mysql/redis -> localhost`）
- 清理 `8080/5173` 端口占用
- 启动前端（5173）和后端（8080）
- 可选把后端启动输出落到 `git-ai/automation-route/runs/backend-dev.log`，便于后续从日志提取验证码

### 1.1)（可选）重置数据库 + 允许调用初始化接口（本地开发环境）

本项目在本地联调/自动化迭代阶段 **允许重置数据库**，也 **允许调用初始化接口**。这主要用于解决以下“环境漂移”：

- `suite` 无法自愈账号资产（`register-seed-users.ps1` 报 `Unable to resolve a working SCHOOL_ADMIN seed account.`）
- 本地跑过 `mvn test` / 集成测试后，非管理员账号被清理，导致 smoke 账号登录 409
- 组织架构/赛事数据被污染，回归难以稳定复跑

推荐优先级（从“最小破坏”到“最大破坏”）：

1) **优先用 API 重置（需要能登录到 SCHOOL_ADMIN/SUPER_ADMIN）**

- 重置接口：`POST /api/admin/school-config/reset`
- 初始化接口：`POST /api/system/init`

2) **若已无法登录任何管理员，再执行数据库级别重置**

- 清空/重建本地库（按你的本地 MySQL 管理方式），然后再调用 `POST /api/system/init` 完成首次初始化。

注意：

- reset 会清理组织架构等基础数据，并可能影响当前回归用数据；执行后必须重新跑 `suite` 让账号资产和赛事窗口重新自愈归一化。
- 初始化接口需要提供新的管理员用户名/密码/邮箱；本地迭代建议用 `school_admin_<suffix>` + `admin123`，便于脚本识别和复用。

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

## 3) 统一执行循环（完整迭代契约）

每轮迭代都按下面的闭环执行，直到“当前已有业务完整性”达到稳定状态：

1. 先读取固定输入：`PROJECT_LOOP.md`、`git-ai/automation-route/BACKLOG.md`、`git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md`、`git-ai/automation-route/BUSINESS_STATUS.md`、`git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md`。
2. 任务选择优先级：先从 `FIRST_BATCH_AUTOMATION_TASKS.md + BUSINESS_STATUS.md` 选“最高优先级、未覆盖、无阻塞”链路，再回落到 backlog 选择规则。
3. 开工后先跑 `suite` 作为基线；若只涉及局部修复，可先定向验证，但收敛前必须回归 `suite`。
4. 失败时必须先定位“具体业务接口 + 根因”，直接修复代码或脚本后重跑，不允许只记录失败不修复。
   - 若失败根因是“本地环境漂移/账号资产不可恢复”，允许执行 **reset/init**（见 1.1），然后重新 `dev-start` + `suite` 走基线回归。
5. 同一问题连续 2 次失败：将对应任务标记为 `BLOCKED`，并写清 `block_reason`（失败信号、根因假设、解除条件）。
6. 全部通过后必须回写：`runs`、`git-ai/automation-route/BUSINESS_STATUS.md`、`git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md`、`git-ai/automation-route/BACKLOG.md`（含 `next_task`）。
7. 每轮结束统一按 `changed_files/key_changes/test_results/risks/next_task` 输出结果。

## 4) 基础回归与完整性回归的关系

- `oneclick` 适合验证本地前后端是否启动正常，以及登录/报名/通知/角色路径基线是否还在。
- `suite` 适合验证“当前项目已有业务完整性”，因为它会在 `oneclick` 通过后继续回放当前赛事的核心业务链。
- 当修复只影响单个接口时，可以先跑定向验证；准备收敛结论前，仍要回到 `suite`。

## 5) 收工（停止本地服务）

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
```

如本轮在 worktree 修改了 `.cursor` 下本地文件，收工后回写到主仓：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sync-local-cursor.ps1 -Direction toMain
```

## 6) 建议的新对话提示词

```text
切到 git-ai/automation-route 分支（若主仓库不在该分支，则进入对应 worktree，例如 D:\soft\lz_sports_git_ai）。
读取 PROJECT_LOOP.md、git-ai/automation-route/BACKLOG.md、git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md、git-ai/automation-route/BUSINESS_STATUS.md、git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md，按统一执行循环开始迭代：
1) 先选最高优先级、未覆盖、无阻塞业务链路（默认 AUTO-042）
2) 执行 dev-start
3) 优先执行 suite（oneclick + event-workflow），按需要扩展到对应业务域定向验证
4) 失败先定位接口和根因并直接修复后重跑；连续 2 次失败标记 BLOCKED 并写 block_reason
5) 通过后回写 runs、BUSINESS_STATUS、FIRST_BATCH、BACKLOG（含 next_task）
6) 执行分支标准 4 步同步，验证 master 与 git-ai/automation-route hash 一致
7) 按 changed_files/key_changes/test_results/risks/next_task 汇报
8) 结束时执行 dev-stop（若有本地 .cursor 变更，再执行 sync-local-cursor toMain）
```

## 7) 优化后的可复用提示词（推荐）

### 7.1 开工 + 执行闭环提示词

```text
在 D:\soft\lz_sports_git_ai 的 git-ai/automation-route worktree 执行（不要在 D:\soft\lz_sports 主仓跑整轮回归）。
先执行 scripts/aicoding-precheck.ps1（或 scripts/test.bat aicoding-precheck），通过后再继续。
先读取 PROJECT_LOOP.md、git-ai/automation-route/BACKLOG.md、git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md、git-ai/automation-route/BUSINESS_STATUS.md、git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md。
从 FIRST_BATCH + BUSINESS_STATUS 选择最高优先级、未覆盖、无阻塞业务链路（默认 AUTO-042），给出 3-5 步计划后直接实施：
1) 执行 dev-start
2) 优先执行 suite（oneclick + event-workflow），按需要补该业务域定向验证
3) 失败先定位具体接口与根因，直接修复并重跑
   - 若失败信号为“账号资产无法自愈 / SCHOOL_ADMIN seed 无法登录 / 本地数据污染导致无法稳定复跑”，允许执行 reset/init（见 WORKFLOW 1.1），再重新 dev-start + suite
   - 同一问题连续 2 次失败则标记 BLOCKED 并写 block_reason
4) 通过后回写 runs、BUSINESS_STATUS、FIRST_BATCH、BACKLOG（含 next_task）
5) 按 changed_files/key_changes/test_results/risks/next_task 汇报
6) 最后执行 dev-stop
```

### 7.3 发布提示词（懒人版）

```text
执行发布流程（脚本已统一到 scripts）：
1) 先执行 scripts/test.bat aicoding-precheck
2) 执行 scripts/test.bat publish（或 scripts/publish.ps1）
3) 汇报发布版本、容器状态和回滚点
```

### 7.2 同步提示词（标准 4 步）

```text
按标准 4 步完成分支同步，并在每一步输出结果：
1) 在 D:\soft\lz_sports_git_ai 完成一轮任务并提交到 git-ai/automation-route
2) 在 D:\soft\lz_sports 执行 git checkout master && git merge git-ai/automation-route
3) 回到 D:\soft\lz_sports_git_ai 执行 git merge master，冲突按路径真源规则解决（例如 git-ai/automation-route/BACKLOG.md）
4) 执行 git rev-parse master 与 git rev-parse git-ai/automation-route，确认 hash 一致
```
