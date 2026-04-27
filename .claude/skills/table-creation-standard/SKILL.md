---
name: table-creation-standard
description: 数据库表与实体生成器。当用户需要设计新表、创建数据库实体、编写 CREATE TABLE 语句或生成对应的 Java POJO/Entity 类时使用。确保字段命名、类型及注解符合项目规范。
---

# 表结构与实体生成规范

## 使用场景

当用户请求：
- "创建一个用户表"
- "添加订单实体"
- "设计一个积分系统的数据库表"

请依据以下规范生成 SQL 和 Java 代码。

## 1. 数据库建表规范 (MySQL)

### 1.1 命名规范
- **表名**：使用 `snake_case`（蛇形命名）
- **前缀**：业务表强制使用 `t_` 前缀（例如 `t_sys_user`）
- **字段名**：使用 `snake_case`
- **关键字**：严禁使用 MySQL 保留字（如 `order`, `desc`, `user` 等）

### 1.2 主键规范
- **名称**：`id`
- **类型**：推荐 `BIGINT`
- **属性**：`AUTO_INCREMENT PRIMARY KEY`

### 1.3 本项目强制字段

本项目使用 MyBatis-Plus 逻辑删除，实体继承 `BaseEntity`：

```sql
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除'
```

### 1.4 配置规范
- **引擎**：`InnoDB`
- **字符集**：`utf8mb4`
- **注释**：表注释和字段注释都必须包含

### 1.5 SQL 示例

```sql
CREATE TABLE IF NOT EXISTS t_example_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',

    -- 业务字段
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '金额',

    -- 通用字段（与 BaseEntity 对应）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除',

    -- 索引
    UNIQUE KEY uk_name (name),
    INDEX idx_user_id (user_id)
) COMMENT='示例实体表';
```

---

## 2. Java Entity 映射规范

### 2.1 类定义
- **位置**：`com.lz.entity` 包下
- **命名**：`PascalCase`，移除表名前缀（如 `t_sys_user` → `SysUser`）
- **父类**：继承 `BaseEntity`（提供 id, createTime, updateTime）或 `SchoolRelatedEntity`（增加 schoolId）
- **注解**：
  - `@Data` (Lombok)
  - `@TableName("t_xxx")` (MyBatis-Plus)

### 2.2 字段定义
- **命名**：`camelCase`
- **主键**：`@TableId(type = IdType.AUTO)`
- **逻辑删除**：`@TableLogic` 标注 deleted 字段（MyBatis-Plus 自动处理）
- **枚举**：使用枚举类型字段，MyBatis-Plus 通过 `@EnumValue` 自动映射

### 2.3 Java 代码示例

```java
package com.lz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 示例实体表
 */
@Data
@TableName("t_example_entity")
public class ExampleEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private Long userId;
    private BigDecimal amount;
}
```

> 注意：`deleted` 逻辑删除字段和 `id`/`createTime`/`updateTime` 审计字段已在 `BaseEntity` 中定义，子类无需重复声明。
