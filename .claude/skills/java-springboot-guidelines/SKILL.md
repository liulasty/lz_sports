---
name: java-springboot-guidelines
description: Java Spring Boot 项目开发全能指南。当用户涉及编写 Java 代码、Controller/Service/Mapper 分层设计、新功能开发、代码重构或询问最佳实践时，必须调用此 Skill 以确保符合项目规范。
---

# Java Spring Boot 开发全案规范

## 适用范围

- **编码实现**：编码风格、分层实现、API 设计
- **新功能开发**：实现新业务功能
- **重构与优化**：优化代码或处理模糊需求

---

## 第一部分：工程与代码基线

### 1. 技术栈约定（本项目）
- **语言**：Java 17
- **框架**：Spring Boot 3.2.3
- **ORM**：MyBatis-Plus 3.5.5
- **工具库**：Lombok, Fastjson2, EasyExcel
- **API文档**：SpringDoc OpenAPI + Knife4j

### 2. 命名规范
- **类名**：`PascalCase` (e.g., `UserController`)
- **方法/变量**：`camelCase` (e.g., `getUserById`)
- **常量**：`UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`)
- **包名**：全小写 (e.g., `com.lz.controller`)
- **Service 实现类**：以 `Impl` 结尾 (e.g., `UserServiceImpl`)

### 3. 分层架构规范

严格遵守标准分层，禁止跨层调用：

```
Controller (Web层) → Service (业务层) → Mapper (数据层) → Database
```

- **Controller**: 仅处理 HTTP 请求/响应，参数校验。**禁止包含复杂业务逻辑**。
- **Service**: 核心业务逻辑，事务边界。**禁止直接返回 Entity 给前端**（应转换为 VO/DTO）。
- **Mapper**: 数据持久化。**禁止在 Service 层拼写 SQL**。

### 4. 本项目特定规范

- **实体继承**：业务实体继承 `BaseEntity`（包含 id, createTime, updateTime）或 `SchoolRelatedEntity`（增加 schoolId）
- **逻辑删除**：使用 MyBatis-Plus `deleted` 字段（0=正常, 1=已删除）
- **角色检查**：使用 `@RequireRole` 和 `@RequireEventAdmin` 注解，通过 `PermissionAspect` 切面执行
- **用户上下文**：通过 `BaseContext.getCurrentId()` 获取当前用户 ID（JWT 过滤器中设置）
- **统一响应**：使用 `Result<T>` 和 `PageResult<T>` 包装返回值

---

## 第二部分：新功能开发流程

> **原则：稳定性 > 优雅；可回滚 > 快速交付**

### 阶段 1：需求去模糊化
在写代码前，必须明确：
1. **定义**：这是新增能力还是修改旧能力？
2. **影响**：是否影响旧接口、旧数据？
3. **产出**：简单的流程图或接口定义。
*若无法回答，禁止开始编码。*

### 阶段 2：影响面评估
- **A类 (纯新增)**：不影响旧逻辑 → 放心开发
- **B类 (复用)**：复用旧逻辑，有分支 → 需覆盖回归测试
- **C类 (重构)**：修改核心链路 → 需极其谨慎，必须有开关或回滚方案

### 阶段 3：隔离式设计
新增功能尽量采用：
- 新接口（v2）
- 新 Service 方法
- 策略模式（Strategy）扩展
- *禁止直接在旧方法中堆砌复杂的 if/else*

### 阶段 4：向后兼容
- 数据库新增字段必须有**默认值**
- 接口新增参数必须**非必填**
- **严禁**直接修改现有接口的返回结构

---

## 第三部分：异常与稳定性

- 所有异常必须被全局异常处理器 (`GlobalExceptionHandler`) 捕获
- **禁止**向前端直接暴露 Java 堆栈信息或数据库错误细节
- 业务异常抛出 `BusinessException` 并附带中文提示
- 当需求不清晰时：保守实现、拆小能力、预留扩展

> **总结**：本规范不仅是代码标准，更是事故预防手册。请在每一次代码提交前对照检查。
