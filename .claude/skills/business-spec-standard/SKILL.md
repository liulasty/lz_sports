---
name: business-spec-standard
description: 业务需求分析与设计专家。当用户提出模糊的新功能需求、需要编写 PRD、设计业务流程或进行系统分析时，优先调用此 Skill 将需求转化为技术规格书。
---

# Business Specification Standard

## Overview

本 Skill 旨在规范业务说明书的编写过程，并建立业务需求与 API 文档之间的清晰映射。帮助开发者从模糊的需求过渡到具体的 API 定义。

## Workflow

### Step 1: 编写业务说明书

使用提供的模板来捕捉业务背景、流程、功能需求和数据模型。

- **Template**: [references/spec-template.md](references/spec-template.md)
- **Output**: 将生成的业务说明书保存在 `docs/specs/` 目录下。
- **Key Elements**:
  - 业务背景
  - 业务流程图 (Mermaid)
  - 功能需求
  - 数据模型
  - 接口清单

### Step 2: 映射 API

在业务说明书完成后，根据业务流程和实体识别必要的 API。

- **Guide**: [references/api-mapping.md](references/api-mapping.md)
- **Action**: 完善业务说明书中的"接口清单"部分，定义 Method 和 Path。

### Step 3: 生成 API 文档

对于清单中的每个 API，生成详细的技术文档。

- **Standard**: 遵循 `api-doc-standard` Skill。
- **Action**: 使用 `api-doc-standard` 为每个接口生成详细定义。

## Resources

- [Business Spec Template](references/spec-template.md) - 业务说明书标准模板
- [API Mapping Guide](references/api-mapping.md) - 业务到 API 的映射指南
