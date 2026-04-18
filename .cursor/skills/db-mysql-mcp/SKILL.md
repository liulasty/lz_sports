---
name: db-mysql-mcp
description: Enable safe MySQL read/write via MCP for this repo (local MySQL + env-local secrets).
---

# LZ Sports 数据库操作（MySQL + MCP）

本 Skill 用于在 **本机 MySQL** 上通过 **MCP** 安全执行 SQL（查询/修改），并把连接信息放在**本地环境变量/.env（不提交 git）**。

## 0. 关键原则（必须遵守）

- **默认只读**：先 `SELECT` 验证范围与影响行，再执行写操作。
- **写操作必须可回滚**：优先使用事务 `BEGIN; ...; COMMIT;`，必要时 `ROLLBACK;`。
- **禁止无条件全表更新/删除**：`UPDATE/DELETE` 必须带 `WHERE`，且先用同条件 `SELECT COUNT(*)`。
- **限制影响范围**：能 `LIMIT` 的场景尽量加；批量更新要分批。
- **记录变更**：每次写操作输出影响行数；必要时先备份关键表（导出/快照）。

## 1. 需要安装/启用的组件

### A) Cursor MCP（必须）

在 Cursor 的 MCP 配置中添加一个 MySQL server（推荐 npm 包之一）：

- `@berthojoris/mcp-mysql-server`（2026 仍在更新）

安装方式通常用 `npx` 即可，无需全局安装。

> 注意：Cursor 的 MCP 配置位置以你的 Cursor 版本为准（通常在 Cursor Settings / MCP 中配置）。

### B) 可选：数据库可视化插件（非必须但强烈推荐）

- VSCode/Cursor 扩展：`SQLTools` + `SQLTools MySQL/MariaDB`
  - 用于你自己可视化看表结构、快速验证结果

## 2. 本地连接信息（不提交 git）

推荐使用本地 `.env` 或系统环境变量，示例：

```
LZ_DB_HOST=127.0.0.1
LZ_DB_PORT=3306
LZ_DB_USER=root
LZ_DB_PASSWORD=your_password
LZ_DB_NAME=lz_sports
```

**不要把真实密码提交到仓库。**

## 3. MCP 配置模板（示例）

以 `@berthojoris/mcp-mysql-server` 为例（把环境变量替换成你的实际值）：

```
mysql://$LZ_DB_USER:$LZ_DB_PASSWORD@$LZ_DB_HOST:$LZ_DB_PORT/$LZ_DB_NAME
```

建议给 MCP server 配置权限分组（至少包含 query/execute/schema 之类能力），并启用“需要确认的写操作”策略（如果该 server 支持）。

## 4. 写操作标准流程（强制）

1) **定位与计数**
- `SELECT ... WHERE ...`
- `SELECT COUNT(*) ... WHERE ...`

2) **事务执行**
- `BEGIN;`
- `UPDATE/INSERT/DELETE ... WHERE ...;`
- 再 `SELECT` 校验结果
- `COMMIT;`

3) **回滚策略**
- 发现条件不对或影响范围不对：`ROLLBACK;` 并重新审查条件

