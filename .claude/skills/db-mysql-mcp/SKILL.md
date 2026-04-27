---
name: db-mysql-mcp
description: Enable safe MySQL read/write via MCP for this repo (local MySQL + env-local secrets). Use this skill when the user needs to query or modify the lz_sports database directly, inspect table structures, or perform data fixes. All write operations MUST follow the safe-write protocol: SELECT first, transaction wrapping, rollback strategy.
---

# LZ Sports 数据库操作（MySQL + MCP）

本 Skill 用于在**本机 MySQL** 上通过 **MCP** 安全执行 SQL（查询/修改），连接信息放在**本地环境变量/.env（不提交 git）**。

## 0. 关键原则（必须遵守）

- **默认只读**：先 `SELECT` 验证范围与影响行，再执行写操作。
- **写操作必须可回滚**：优先使用事务 `BEGIN; ...; COMMIT;`，必要时 `ROLLBACK;`。
- **禁止无条件全表更新/删除**：`UPDATE/DELETE` 必须带 `WHERE`，且先用同条件 `SELECT COUNT(*)`。
- **限制影响范围**：能 `LIMIT` 的场景尽量加；批量更新要分批。
- **记录变更**：每次写操作输出影响行数；必要时先备份关键表。

## 1. MCP Server 配置

### 已配置的 MCP Server

本项目已通过以下命令注册 MCP server：

```bash
CLAUDE_CODE_GIT_BASH_PATH="D:/CODE/Git/bin/bash.exe" claude mcp add lz-sports-mysql \
  npx -- -y @executeautomation/database-server \
  --mysql --host localhost --database lz_sports --port 3306 --user root --password 1234
```

- **MCP Server 名称**: `lz-sports-mysql`
- **包**: `@executeautomation/database-server`
- **配置存储**: `C:\Users\Administrator\.claude.json` (project scope: `D:\soft\lz_sports`)
- **连接信息来源**: `config/.env.dev`（`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`, `DB_PORT`）

> **注意**: 数据库密码存储在 `config/.env.dev` 中，该文件已在 `.gitignore` 排除。MCP 配置中的密码是明文存储的，因此 `.claude.json` 也加入了 `.gitignore`。

### 如何查看 MCP 状态

```bash
claude mcp list
```

### 如何重新添加

如果 MCP 配置丢失或更换电脑，从 `config/.env.dev` 读取配置后执行：

```bash
claude mcp add lz-sports-mysql \
  npx -- -y @executeautomation/database-server \
  --mysql \
  --host localhost \
  --database lz_sports \
  --port 3306 \
  --user root \
  --password <密码>
```

> Windows 用户需要先设置 `CLAUDE_CODE_GIT_BASH_PATH` 环境变量指向 Git Bash 路径（本项目为 `D:/CODE/Git/bin/bash.exe`）。

## 2. 本项目数据库概览

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sys_user` | 用户表 | `user_type` (角色), `status` (状态), `deleted` |
| `athlete` | 运动员认证 | `user_id`, `event_id`, `athlete_state` |
| `registration` | 报名记录 | `athlete_id`, `event_id`, `item_id`, `status` |
| `result` | 成绩表 | `registration_id`, `event_id`, `item_id`, `is_published` |
| `event` | 赛事表 | `status` (DRAFT/OPEN/CLOSED/ONGOING/FINISHED) |
| `event_item` | 项目表 | `event_id`, `current_count`, `max_count` |
| `department` | 组织架构 | `college`, `major`, `grade`, `class_name`, `org_mode` |
| `school_config` | 学校配置 | `is_initialized`, `school_name`, `org_mode` |
| `notification` | 通知 | `user_id`, `type`, `is_read` |
| `event_admin_mapping` | 赛事管理员映射 | `event_id`, `user_id` |

**逻辑删除字段**：`deleted` (0=正常, 1=已删除)，MyBatis-Plus 自动过滤。

## 3. 写操作标准流程（强制）

```
1) 定位与计数
   SELECT * FROM t_xxx WHERE condition;
   SELECT COUNT(*) FROM t_xxx WHERE condition;

2) 事务执行
   BEGIN;
   UPDATE/INSERT/DELETE t_xxx SET ... WHERE condition;
   SELECT 校验结果
   COMMIT;

3) 回滚策略
   发现条件不对或影响范围不对：ROLLBACK; 并重新审查条件
```

## 4. 常用查询模板

### 查看用户角色分布

```sql
SELECT user_type, status, COUNT(*) FROM sys_user WHERE deleted = 0 GROUP BY user_type, status;
```

### 查看赛事报名情况

```sql
SELECT e.name, r.status, COUNT(*)
FROM event e JOIN registration r ON e.id = r.event_id
GROUP BY e.name, r.status;
```

### 查看运动员申请状态

```sql
SELECT e.name AS event_name, a.athlete_state, COUNT(*)
FROM event e JOIN athlete a ON e.id = a.event_id
GROUP BY e.name, a.athlete_state;
```

### 检查数据关联完整性

```sql
-- 孤立的报名记录（关联的赛事不存在）
SELECT r.id, r.event_id FROM registration r
LEFT JOIN event e ON r.event_id = e.id
WHERE e.id IS NULL;
```
