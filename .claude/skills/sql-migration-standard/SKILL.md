---
name: sql-migration-standard
description: 数据库变更管理工具。当用户需要修改数据库结构（如建表、加字段、改索引）或进行数据订正时，必须调用此 Skill 生成标准化的 SQL 迁移文件，严禁直接手写 SQL。
---

# SQL Migration Standard

本 Skill 用于生成标准化的数据库变更 SQL 文件。

## 使用流程

### 1. 生成 SQL 迁移文件

当需要对数据库进行变更时，**不要**直接编写 SQL 或修改现有的 SQL 文件。请执行以下步骤：

1. 确定变更的目的（例如："create user table", "add age column"）。
2. 确定 SQL 文件的存放目录。本项目的迁移文件存放在 `lz_sports_backend/src/main/resources/sql/` 目录下。
3. 运行脚本生成文件：

```bash
python .claude/skills/sql-migration-standard/scripts/generate_migration.py --purpose "<变更目的>" --dir "<存放目录>"
```

### 2. 编写 SQL 内容

文件生成后，按以下要求填充内容：

1. **Header 信息**：脚本已自动生成 Header。请在 `Description` 部分补充详细的变更说明。
2. **SQL 语句**：在 `-- Write your SQL commands below this line` 下方编写具体的 SQL 语句。
3. **规范检查**：
   - 建表语句必须包含完整的注释（表注释和字段注释）
   - 删除操作（DROP/DELETE）必须谨慎，在 Description 中详细说明原因
   - 确保 SQL 语句以分号 `;` 结尾

## 示例

**生成文件：**

```bash
python .claude/skills/sql-migration-standard/scripts/generate_migration.py --purpose "add_user_status" --dir "lz_sports_backend/src/main/resources/sql"
```

**输出文件示例 (20231027103000_add_user_status.sql)：**

```sql
/*
 * Migration: 20231027103000_add_user_status
 * Date: 2023-10-27 10:30:00
 * Purpose: add_user_status
 *
 * Operations:
 * [ ] Add
 * [ ] Delete
 * [x] Adjust
 *
 * Description:
 * Add status column to t_user table to support soft delete.
 */

-- Write your SQL commands below this line
-- ---------------------------------------

ALTER TABLE t_user ADD COLUMN status TINYINT(1) DEFAULT 1 COMMENT 'Status: 1-Active, 0-Deleted';
```
