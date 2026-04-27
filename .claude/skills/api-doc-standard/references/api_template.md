# {Interface Name}

## 1. 接口描述
{简要描述接口的功能和使用场景}

## 2. 请求信息
- **URL**: `{path}`
- **Method**: `{GET/POST/PUT/DELETE}`
- **Content-Type**: `application/json`

## 3. 请求参数

### Path Parameters
| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| {id} | string | 是 | {描述} |

### Query Parameters
| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| {page} | int | 否 | 页码 |

### Request Body
| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| {username} | string | 是 | 用户名 |

**Request Example:**
```json
{
  "username": "test_user"
}
```

## 4. 响应信息

### Response Body
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| code | int | 状态码 (0表示成功) |
| message | string | 提示信息 |
| data | object | 业务数据 |

**Response Example (Success):**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "123",
    "username": "test_user"
  }
}
```

## 5. 错误码
参考全局错误码文档。
