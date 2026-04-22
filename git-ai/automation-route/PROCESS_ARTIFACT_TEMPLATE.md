# 过程产物模板（可泛化）

用于每次自动化回合后统一落盘，减少重复叙述与文档漂移。

## A. 任务状态真源（必须）

- 文件：`BACKLOG.md`
- 仅更新：
  - 任务 `status`
  - `block_reason`
  - `dependencies`
  - `next_task`

## B. 业务状态摘要（必须）

- 文件：`BUSINESS_STATUS.md`
- 仅保留最近 1~3 轮关键结论：
  - 本轮目标
  - 本轮结果（PASS/FAIL）
  - 若失败：根因与修复动作
  - 下一任务

## C. 批次文档（可选，轻量）

- 文件：`FIRST_BATCH_AUTOMATION_TASKS.md` / `SECOND_BATCH_AUTOMATION_TASKS.md`
- 原则：
  - 只保留批次定义、完成态与衔接关系
  - 不重复贴每轮运行细节
  - 详细轨迹放 `runs/*.md`

## D. 运行记录（明细）

- 文件：`runs/YYYY-MM-DD-auto-xxx.md`
- 每次执行完整保留，作为审计明细与复盘依据。

## E. 推荐最小回写格式

```yaml
round: 2026-04-20-auto-xxx
task: AUTO-xxx
result: PASS|FAIL
fix_applied: true|false
suite_regression: PASS|FAIL
next_task: AUTO-yyy
```
