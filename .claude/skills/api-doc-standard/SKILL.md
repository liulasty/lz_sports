---
name: api-doc-standard
description: API 文档专家。当用户需要编写、更新、审查或补全 RESTful API 文档（接口说明、Swagger/OpenAPI 定义）时使用。支持从代码反向生成文档或根据需求设计接口文档。
---

# API Documentation Standard

## Overview

本 Skill 旨在规范 API 文档的创建过程，确保生成的文档清晰、一致且包含所有必要信息。适用于 RESTful API 文档的生成。

## Core Guidelines

在编写 API 文档时，请始终遵循以下核心原则：

1. **清晰性**：使用简洁明了的语言描述接口功能。
2. **完整性**：包含请求方法、URL、参数、请求体、响应体和错误码。
3. **一致性**：遵循统一的 Markdown 格式和命名约定。
4. **示例**：为每个接口提供具体的请求和响应 JSON 示例。

## Usage

### Creating New API Docs

当用户请求为某个功能或代码创建 API 文档时：

1. **分析代码/需求**：理解接口的输入、输出和业务逻辑。
2. **使用模板**：参考 [api_template.md](references/api_template.md) 中的结构。
3. **填写详情**：
   - **接口描述**：简要说明接口用途。
   - **HTTP 方法与路径**：明确 GET/POST/PUT/DELETE 及 URL。
   - **请求参数**：列出 Path、Query 和 Header 参数。
   - **请求体**：详细说明 JSON 字段及其类型、是否必填。
   - **响应体**：详细说明成功和失败的响应结构。
   - **示例**：提供真实的 Request/Response 示例。
4. **保存位置**：将生成的 API 文档保存在 `docs/api/` 目录下。

### Reviewing API Docs

当用户请求审查现有文档时，检查是否遗漏以下内容：
- 错误码定义（参考 [status_codes.md](references/status_codes.md)）
- 字段类型说明
- 必填项标记
- 边界条件说明

## Project-Specific Conventions

本项目的 API 命名和响应格式遵循以下约定：

- 路径前缀：`/api/` + 领域名（如 `/api/auth`, `/api/event`, `/api/admin/*`）
- 后端统一响应格式：`Result<T>` 包装，包含 `code`, `msg`, `data`
- 分页响应：`PageResult<T>`，包含 `total` 和 `records`
- 认证方式：`token` header 或 `Authorization: Bearer <token>`

## Resources

- **[Template](references/api_template.md)**: 标准 API 文档 Markdown 模板
- **[Status Codes](references/status_codes.md)**: 常用 HTTP 状态码及业务错误码规范
