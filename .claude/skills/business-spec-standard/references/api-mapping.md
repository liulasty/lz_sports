# API 映射指南 (API Mapping Guide)

本指南说明如何将业务需求转化为 API 定义，并与 `api-doc-standard` 配合使用。

## 1. 接口识别原则

根据业务流程图和功能需求，识别需要的 API 接口：

- **资源 (Resource)**: 识别业务实体（如 Order, User, Product）。
- **动作 (Action)**: 识别对实体的操作（Create, Read, Update, Delete, Execute）。

## 2. 接口定义规范

- **RESTful 风格**: 使用标准的 HTTP 方法 (GET, POST, PUT, DELETE)。
- **路径命名**: 使用小写，连字符分隔 (e.g., `/api/v1/orders`)。
- **本项目前缀**: 所有 API 统一使用 `/api/` 前缀。

## 3. 关联 API 文档

完成业务说明书后，使用 `api-doc-standard` Skill 生成详细的 API 文档。

**工作流**:
1. 在业务说明书中完成"接口清单"。
2. 对于清单中的每个 API，使用 `api-doc-standard` 生成详细文档。
3. 确保 API 文档包含：请求/响应示例、错误码定义、字段详细说明。

## 4. 示例

**业务需求**: 用户下单。
- **API 识别**: Resource: Order, Action: Create
- **API 定义**: Method: `POST`, Path: `/api/v1/orders`
