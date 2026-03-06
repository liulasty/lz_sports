## 接口设计

### 统一规范（所有接口遵守）

**请求规范**

```
Base URL：/api
Content-Type：application/json
认证方式：Header 携带 Authorization: Bearer {token}
```

**统一响应格式**

json

```
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**统一错误码**

| code | 含义    | 说明             |
| ---- | ----- | -------------- |
| 200  | 成功    | -              |
| 400  | 参数错误  | message 说明具体原因 |
| 401  | 未登录   | token 不存在或已过期  |
| 403  | 无权限   | 角色不符或越权操作      |
| 404  | 资源不存在 | -              |
| 409  | 业务冲突  | 重复操作、状态不符等     |
| 500  | 服务器错误 | -              |

**分页请求统一格式**

json

```
{
  "page": 1,
  "size": 10
}
```

**分页响应统一格式**

json

```
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
​```

---

## 第一批：系统初始化 & 认证（9个接口）

---

### 接口1：查询系统初始化状态
​```
GET /api/system/init-status
权限：无需登录
```

**响应**

json

```
{
  "code": 200,
  "data": {
    "initialized": false
  }
}
​```

**业务规则**
- 前端每次加载时调用此接口
- initialized = false 时强制跳转初始化向导页
- initialized = true 时正常进入登录页

---

### 接口2：执行系统初始化
​```
POST /api/system/init
权限：无需登录（仅未初始化时有效）
```

**请求参数**

| 字段         | 类型     | 必填   | 校验规则    | 说明    |
| ---------- | ------ | ---- | ------- | ----- |
| schoolName | string | ✅    | 长度2-100 | 学校名称  |
| themeCode  | string | ✅    | 枚举值之一   | 主题色代码 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "adminAccount": "admin",
    "adminPassword": "Admin@123",
    "message": "初始化成功，请立即登录并修改密码"
  }
}
​```

**业务规则**
- 已初始化时调用返回 409
- 整个操作在一个事务中完成：写入 school_config + 创建超管账号 + 写入内置项目库
- Logo 不在此接口上传，初始化完成后在系统配置页单独上传

---

### 接口3：发送邮箱验证码
​```
POST /api/auth/send-code
权限：无需登录
```

**请求参数**

| 字段    | 类型     | 必填   | 校验规则                      | 说明   |
| ----- | ------ | ---- | ------------------------- | ---- |
| email | string | ✅    | 合法邮箱格式                    | 目标邮箱 |
| scene | string | ✅    | REGISTER / RESET_PASSWORD | 使用场景 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "cooldown": 60
  }
}
​```

**业务规则**
- 同一邮箱 60 秒内不可重复发送，违反返回 409 并告知剩余冷却秒数
- 验证码6位数字，有效期 10 分钟，存入 Redis（key: code:{scene}:{email}）
- REGISTER 场景：邮箱已注册时返回 409
- RESET_PASSWORD 场景：邮箱未注册时返回 404

---

### 接口4：验证邮箱验证码
​```
POST /api/auth/verify-code
权限：无需登录
```

**请求参数**

| 字段    | 类型     | 必填   | 校验规则                      | 说明   |
| ----- | ------ | ---- | ------------------------- | ---- |
| email | string | ✅    | 合法邮箱格式                    | 邮箱   |
| code  | string | ✅    | 6位数字                      | 验证码  |
| scene | string | ✅    | REGISTER / RESET_PASSWORD | 使用场景 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "verifyToken": "uuid-xxx"
  }
}
​```

**业务规则**
- 验证通过后返回 verifyToken，有效期 30 分钟，存入 Redis
- 注册和重置密码接口必须携带此 verifyToken 才能操作
- 验证码错误返回 400，验证码过期返回 400（提示重新发送）
- verifyToken 使用一次后立即失效

---

### 接口5：用户注册
​```
POST /api/auth/register
权限：无需登录
```

**请求参数**

| 字段          | 类型     | 必填   | 校验规则         | 说明         |
| ----------- | ------ | ---- | ------------ | ---------- |
| email       | string | ✅    | 合法邮箱格式       | 注册邮箱       |
| password    | string | ✅    | 8-20位，含字母和数字 | 登录密码       |
| verifyToken | string | ✅    | 非空           | 邮箱验证通过后的凭证 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "userId": 10001,
    "email": "user@example.com",
    "role": "USER"
  }
}
​```

**业务规则**
- verifyToken 无效或过期返回 400
- 注册成功后账号直接激活，无需审核
- 密码 BCrypt 加密存储

---

### 接口6：用户登录
​```
POST /api/auth/login
权限：无需登录
```

**请求参数**

| 字段       | 类型     | 必填   | 校验规则 | 说明   |
| -------- | ------ | ---- | ---- | ---- |
| email    | string | ✅    | 非空   | 登录邮箱 |
| password | string | ✅    | 非空   | 登录密码 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "userId": 10001,
    "role": "USER",
    "isFirstLogin": false,
    "unreadCount": 3
  }
}
​```

**业务规则**

| 账号状态 | 返回结果 |
|---------|---------|
| 正常 | 登录成功，返回 token |
| 禁用 | 400，提示「账号已被禁用，请联系管理员」 |
| 首次登录（超管） | 登录成功，isFirstLogin=true，前端强制跳转改密码页 |

- token 有效期 7 天
- 密码错误不提示是邮箱不存在还是密码错误，统一提示「邮箱或密码错误」

---

### 接口7：修改密码
​```
POST /api/auth/change-password
权限：需要登录
​```

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|-----|-----|-----|---------|-----|
| oldPassword | string | ✅ | 非空 | 原密码 |
| newPassword | string | ✅ | 8-20位，含字母和数字 | 新密码 |

**业务规则**
- 修改成功后当前 token 立即失效，前端跳转登录页
- 超管首次登录改密码后，is_first_login 置为 0

---

### 接口8：重置密码
​```
POST /api/auth/reset-password
权限：无需登录
​```

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|-----|-----|-----|---------|-----|
| email | string | ✅ | 合法邮箱格式 | 账号邮箱 |
| newPassword | string | ✅ | 8-20位，含字母和数字 | 新密码 |
| verifyToken | string | ✅ | 非空 | 邮箱验证凭证 |

**业务规则**
- verifyToken 无效或过期返回 400
- 重置成功后所有已登录的 token 立即失效

---

### 接口9：获取当前登录用户信息
​```
GET /api/auth/me
权限：需要登录
```

**响应**

json

```
{
  "code": 200,
  "data": {
    "userId": 10001,
    "email": "user@example.com",
    "role": "USER",
    "realName": "张三",
    "department": "计算机学院",
    "grade": "大三",
    "unreadCount": 3
  }
}
​```

---

## 第二批：公开信息 & 通知（7个接口）

---

### 接口10：获取学校公开配置
​```
GET /api/public/school-config
权限：无需登录
```

**响应**

json

```
{
  "code": 200,
  "data": {
    "schoolName": "XX大学",
    "logoUrl": "/uploads/logo.png",
    "themeCode": "BLUE"
  }
}
​```

**业务规则**
- 前端每次启动时调用，用于渲染 Logo、校名、主题色
- 建议前端做本地缓存，避免频繁请求

---

### 接口11：获取赛事列表（公开）
​```
GET /api/public/events
权限：无需登录
```

**请求参数（Query）**

| 字段     | 类型     | 必填   | 说明               |
| ------ | ------ | ---- | ---------------- |
| status | string | ❌    | 筛选状态，不传返回所有非草稿赛事 |
| page   | int    | ❌    | 默认1              |
| size   | int    | ❌    | 默认10             |

**响应（列表项）**

json

```
{
  "id": 1,
  "name": "2024年秋季运动会",
  "coverUrl": "/uploads/event1.jpg",
  "venue": "田径场",
  "status": "OPEN",
  "regStartTime": "2024-10-01 00:00:00",
  "regEndTime": "2024-10-10 23:59:59",
  "eventStartTime": "2024-10-15 08:00:00",
  "itemCount": 12
}
​```

---

### 接口12：获取赛事详情（公开）
​```
GET /api/public/events/{eventId}
权限：无需登录
```

**响应**

json

```
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "2024年秋季运动会",
    "description": "...",
    "coverUrl": "...",
    "venue": "田径场",
    "status": "OPEN",
    "regStartTime": "...",
    "regEndTime": "...",
    "eventStartTime": "...",
    "eventEndTime": "...",
    "maxItemsPerAthlete": 3,
    "items": [
      {
        "id": 101,
        "name": "男子100米",
        "genderLimit": "MALE",
        "maxParticipants": 50,
        "currentCount": 23,
        "itemStartTime": "2024-10-15 09:00:00",
        "itemEndTime": "2024-10-15 10:00:00",
        "description": ""
      }
    ],
    "myAthleteStatus": "APPROVED",
    "myRegistrations": [101, 103]
  }
}
​```

**业务规则**
- 草稿状态赛事返回 404
- myAthleteStatus 和 myRegistrations 字段：未登录时返回 null，已登录时返回当前用户状态

---

### 接口13：获取赛事公开成绩
​```
GET /api/public/events/{eventId}/results
权限：无需登录
```

**请求参数（Query）**

| 字段     | 类型   | 必填   | 说明    |
| ------ | ---- | ---- | ----- |
| itemId | long | ❌    | 按项目筛选 |

**响应**

json

```
{
  "code": 200,
  "data": [
    {
      "itemId": 101,
      "itemName": "男子100米",
      "results": [
        {
          "rank": 1,
          "realName": "张三",
          "department": "计算机学院",
          "grade": "大三",
          "score": "11.23秒",
          "remarks": ""
        }
      ]
    }
  ]
}
​```

**业务规则**
- 只返回 is_published = 1 的成绩
- 按项目分组，每组内按 rank 升序排列

---

### 接口14：获取我的通知列表
​```
GET /api/notifications
权限：需要登录
```

**请求参数（Query）**

| 字段     | 类型   | 必填   | 说明             |
| ------ | ---- | ---- | -------------- |
| isRead | int  | ❌    | 0未读/1已读，不传返回全部 |
| page   | int  | ❌    | 默认1            |
| size   | int  | ❌    | 默认20           |

**响应（列表项）**

json

```
{
  "id": 1001,
  "title": "运动员申请已通过",
  "content": "您在2024年秋季运动会的运动员申请已通过，快去报名吧！",
  "type": "ATHLETE_APPROVED",
  "relatedId": 1,
  "isRead": 0,
  "createdAt": "2024-10-01 12:00:00"
}
​```

---

### 接口15：标记通知为已读
​```
POST /api/notifications/read
权限：需要登录
​```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| ids | array | ❌ | 通知id列表，不传则标记全部已读 |

**业务规则**
- 标记已读后同步更新 sys_user.unread_count
- 只能标记自己的通知，标记他人通知返回 403

---

### 接口16：获取未读消息数量
​```
GET /api/notifications/unread-count
权限：需要登录
```

**响应**

json

```
{
  "code": 200,
  "data": {
    "count": 5
  }
}
​```

**业务规则**
- 直接读取 sys_user.unread_count 冗余字段，不做实时统计，性能更好
- 导航栏角标轮询此接口（建议30秒一次）

---

## 第三批：运动员申请 & 项目报名（6个接口）

---

### 接口17：申请成为运动员
​```
POST /api/athlete/apply
权限：需要登录（USER角色）
```

**请求参数**

| 字段         | 类型     | 必填   | 校验规则    | 说明      |
| ---------- | ------ | ---- | ------- | ------- |
| eventId    | long   | ✅    | 非空      | 申请的赛事id |
| realName   | string | ✅    | 长度2-50  | 真实姓名    |
| department | string | ❌    | 长度最大100 | 院系      |
| grade      | string | ❌    | 长度最大20  | 年级      |
| phone      | string | ❌    | 手机号格式   | 联系方式    |

**响应**

json

```
{
  "code": 200,
  "data": {
    "applicationId": 2001,
    "status": "PENDING"
  }
}
​```

**业务规则**
- 赛事状态不是 OPEN 时返回 409，提示「该赛事不在报名期内」
- 同一用户同一赛事已有申请记录返回 409
- 申请成功后自动将 realName、department、grade、phone 同步更新到 sys_user 表（方案C）
- SUPER_ADMIN 和 EVENT_ADMIN 角色不可申请

---

### 接口18：查询我的运动员申请状态
​```
GET /api/athlete/my-applications
权限：需要登录
```

**响应（列表项）**

json

```
{
  "applicationId": 2001,
  "eventId": 1,
  "eventName": "2024年秋季运动会",
  "status": "PENDING",
  "rejectReason": null,
  "appliedAt": "2024-10-01 10:00:00",
  "reviewedAt": null
}
​```

---

### 接口19：提交项目报名
​```
POST /api/registrations
权限：需要登录
```

**请求参数**

| 字段          | 类型   | 必填   | 校验规则 | 说明         |
| ----------- | ---- | ---- | ---- | ---------- |
| eventItemId | long | ✅    | 非空   | 要报名的赛事项目id |

**响应**

json

```
{
  "code": 200,
  "data": {
    "registrationId": 3001,
    "eventItemId": 101,
    "itemName": "男子100米",
    "status": "CONFIRMED"
  }
}
​```

**业务规则（按顺序逐一校验，失败立即返回具体原因）**

| 序号 | 校验项 | 失败提示 |
|-----|-------|---------|
| 1 | 账号是否 ACTIVE | 「账号已被禁用」 |
| 2 | 是否已通过该赛事运动员审核 | 「请先申请并通过运动员资格审核」 |
| 3 | 当前时间是否在报名时间内 | 「不在报名时间范围内」 |
| 4 | 该项目是否还有名额 | 「该项目报名人数已满」 |
| 5 | 是否已报名同一项目 | 「您已报名该项目，请勿重复报名」 |
| 6 | 已报名项目数是否超上限 | 「已达到本赛事最多报名X个项目的限制」 |
| 7 | 项目时间是否与已报项目冲突 | 「与您已报名的[项目名]时间冲突」 |

- 名额扣减使用原子操作：`UPDATE event_item SET current_count = current_count + 1 WHERE id = ? AND current_count < max_participants`
- UPDATE 影响行数为 0 时说明名额已满，返回 409

---

### 接口20：取消项目报名
​```
DELETE /api/registrations/{registrationId}
权限：需要登录
​```

**业务规则**
- 只能取消自己的报名，取消他人报名返回 403
- 赛事报名截止后不可取消，返回 409，提示「报名截止后不可取消」
- 取消成功后 event_item.current_count - 1（原子操作）
- 状态置为 CANCELLED，不物理删除

---

### 接口21：查询我的报名记录
​```
GET /api/registrations/my
权限：需要登录
```

**请求参数（Query）**

| 字段      | 类型   | 必填   | 说明    |
| ------- | ---- | ---- | ----- |
| eventId | long | ❌    | 按赛事筛选 |
| page    | int  | ❌    | 默认1   |
| size    | int  | ❌    | 默认10  |

**响应（列表项）**

json

```
{
  "registrationId": 3001,
  "eventId": 1,
  "eventName": "2024年秋季运动会",
  "itemId": 101,
  "itemName": "男子100米",
  "itemStartTime": "2024-10-15 09:00:00",
  "status": "CONFIRMED",
  "registeredAt": "2024-10-02 14:00:00",
  "result": {
    "score": "11.23秒",
    "rank": 1,
    "isPublished": true
  }
}
​```

---

### 接口22：查询我的成绩
​```
GET /api/results/my
权限：需要登录
```

**响应（列表项）**

json

```
{
  "eventId": 1,
  "eventName": "2024年秋季运动会",
  "itemId": 101,
  "itemName": "男子100米",
  "score": "11.23秒",
  "rank": 1,
  "remarks": "",
  "publishedAt": "2024-10-16 18:00:00"
}
​```

**业务规则**
- 只返回 is_published = 1 的成绩

---

## 第四批：管理员接口（20个接口）

---

### 接口23：更新学校配置
​```
PUT /api/admin/school-config
权限：SUPER_ADMIN
​```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| schoolName | string | ❌ | 学校名称 |
| themeCode | string | ❌ | 主题色代码 |

---

### 接口24：上传学校Logo
​```
POST /api/admin/school-config/logo
权限：SUPER_ADMIN
Content-Type：multipart/form-data
```

**请求参数**

| 字段   | 类型   | 必填   | 校验规则             | 说明     |
| ---- | ---- | ---- | ---------------- | ------ |
| file | file | ✅    | jpg/png/gif，≤5MB | Logo图片 |

**响应**

json

```
{
  "code": 200,
  "data": {
    "logoUrl": "/uploads/logo_xxx.png"
  }
}
​```

---

### 接口25：创建赛事
​```
POST /api/admin/events
权限：SUPER_ADMIN
​```

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|-----|-----|-----|---------|-----|
| name | string | ✅ | 长度2-100 | 赛事名称 |
| description | string | ❌ | 最大2000字 | 赛事描述 |
| venue | string | ❌ | 最大200字 | 举办地点 |
| regStartTime | datetime | ✅ | 必须大于当前时间 | 报名开始时间 |
| regEndTime | datetime | ✅ | 必须大于regStartTime | 报名截止时间 |
| eventStartTime | datetime | ✅ | 必须大于regEndTime | 比赛开始时间 |
| eventEndTime | datetime | ✅ | 必须大于eventStartTime | 比赛结束时间 |
| maxItemsPerAthlete | int | ✅ | 1-20 | 每人最多报名项目数 |

**业务规则**
- 创建后状态为 DRAFT
- 时间校验：regStartTime < regEndTime < eventStartTime < eventEndTime

---

### 接口26：编辑赛事
​```
PUT /api/admin/events/{eventId}
权限：SUPER_ADMIN
​```

**业务规则**

| 赛事状态 | 可修改字段 |
|---------|---------|
| DRAFT | 所有字段 |
| OPEN（报名未开始） | 所有字段 |
| OPEN（报名已开始） | description、venue、coverUrl，maxItemsPerAthlete不可改 |
| CLOSED及之后 | 不可编辑，返回 409 |

---

### 接口27：上传赛事封面图
​```
POST /api/admin/events/{eventId}/cover
权限：SUPER_ADMIN
Content-Type：multipart/form-data
​```

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|-----|-----|-----|---------|-----|
| file | file | ✅ | jpg/png，≤5MB | 封面图片 |

---

### 接口28：发布赛事
​```
POST /api/admin/events/{eventId}/publish
权限：SUPER_ADMIN
​```

**业务规则**
- 仅 DRAFT 状态可发布
- 发布前校验：至少有一个 event_item，否则返回 400
- 发布前校验：至少指定一个赛事管理员，否则返回 400
- 发布成功后向所有注册用户发送站内通知（异步）
- 发布后状态变为 OPEN

---

### 接口29：撤回赛事
​```
POST /api/admin/events/{eventId}/withdraw
权限：SUPER_ADMIN
​```

**业务规则**
- 仅 OPEN 且报名未开始时可撤回（当前时间 < regStartTime）
- 撤回后状态变回 DRAFT

---

### 接口30：获取赛事列表（管理员）
​```
GET /api/admin/events
权限：SUPER_ADMIN
​```

**请求参数（Query）**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| status | string | ❌ | 按状态筛选，包含DRAFT |
| keyword | string | ❌ | 按赛事名称模糊搜索 |
| page | int | ❌ | 默认1 |
| size | int | ❌ | 默认10 |

**与公开接口的区别**：包含 DRAFT 状态的赛事

---

### 接口31：删除赛事
​```
DELETE /api/admin/events/{eventId}
权限：SUPER_ADMIN
​```

**业务规则**
- 仅 DRAFT 状态可删除，其他状态返回 409
- 同时删除关联的 event_item 和 event_admin_mapping（级联删除）

---

### 接口32：管理赛事项目
​```
POST   /api/admin/events/{eventId}/items      新增项目
PUT    /api/admin/events/{eventId}/items/{itemId}  编辑项目
DELETE /api/admin/events/{eventId}/items/{itemId}  删除项目
GET    /api/admin/events/{eventId}/items      获取项目列表
权限：SUPER_ADMIN
​```

**新增项目请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|-----|-----|-----|---------|-----|
| projectId | long | ✅ | 非空 | 引用项目库id |
| itemStartTime | datetime | ❌ | 在赛事时间范围内 | 项目比赛开始时间 |
| itemEndTime | datetime | ❌ | 大于itemStartTime | 项目比赛结束时间 |
| maxParticipants | int | ✅ | 1-9999 | 参赛人数上限 |
| genderLimit | string | ✅ | ALL/MALE/FEMALE | 性别限制 |
| description | string | ❌ | 最大500字 | 补充说明 |

**删除业务规则**
- 已有报名记录时不可删除，返回 409，提示「该项目已有X人报名，无法删除」

---

### 接口33：管理赛事管理员
​```
POST   /api/admin/events/{eventId}/admins          指定管理员
DELETE /api/admin/events/{eventId}/admins/{userId}  移除管理员
GET    /api/admin/events/{eventId}/admins           获取管理员列表
权限：SUPER_ADMIN
​```

**指定管理员请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| userIds | array | ✅ | 用户id列表 |

**业务规则**
- 被指定的用户 role 必须是 EVENT_ADMIN 或 SUPER_ADMIN
- 移除时若该赛事只剩一个管理员，返回 400 提示「至少保留一名赛事管理员」

---

### 接口34：获取运动员申请列表（赛事管理员）
​```
GET /api/event-admin/{eventId}/athlete-applications
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
​```

**请求参数（Query）**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| status | string | ❌ | PENDING/APPROVED/REJECTED |
| keyword | string | ❌ | 按姓名或院系搜索 |
| page | int | ❌ | 默认1 |
| size | int | ❌ | 默认20 |

---

### 接口35：审核运动员申请
​```
POST /api/event-admin/{eventId}/athlete-applications/{applicationId}/approve
POST /api/event-admin/{eventId}/athlete-applications/{applicationId}/reject
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
​```

**拒绝请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| rejectReason | string | ✅ | 拒绝原因，不能为空 |

**业务规则**
- 审核通过后发送站内通知（异步）
- 审核拒绝后发送站内通知（含拒绝原因，异步）
- 已审核的申请不可重复审核，返回 409

---

### 接口36：批量审核运动员申请
​```
POST /api/event-admin/{eventId}/athlete-applications/batch-approve
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
​```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| applicationIds | array | ✅ | 申请id列表 |

**业务规则**
- 只处理状态为 PENDING 的申请，已审核的跳过
- 返回处理结果：成功N条，跳过N条

---

### 接口37：成绩管理
​```
POST /api/event-admin/{eventId}/results              录入成绩
PUT  /api/event-admin/{eventId}/results/{resultId}   修改成绩
GET  /api/event-admin/{eventId}/results              获取成绩列表（含草稿）
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
​```

**录入成绩请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| itemRegistrationId | long | ✅ | 报名记录id |
| score | string | ❌ | 成绩 |
| rank | int | ❌ | 排名 |
| remarks | string | ❌ | 备注 |

**业务规则**
- 只能为 CONFIRMED 状态的报名录入成绩
- 重复录入时更新已有记录（upsert）
- 已发布的成绩不可修改，返回 409

---

### 接口38：发布成绩
​```
POST /api/event-admin/{eventId}/results/publish
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
​```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| itemId | long | ❌ | 不传则发布该赛事全部项目成绩 |

**业务规则**
- 发布后 is_published 置为 1，published_at 记录时间
- 发布后不可撤回，前端弹二次确认弹窗
- 发布后向该赛事所有运动员发送站内通知（异步）

---

### 接口39：Excel成绩导入导出
​```
GET  /api/event-admin/{eventId}/results/template   下载导入模板
POST /api/event-admin/{eventId}/results/import     批量导入成绩
GET  /api/event-admin/{eventId}/results/export     导出成绩表
GET  /api/event-admin/{eventId}/registrations/export  导出报名名单
权限：EVENT_ADMIN（且绑定该eventId）或 SUPER_ADMIN
```

**导入响应**

json

```
{
  "code": 200,
  "data": {
    "successCount": 45,
    "failCount": 3,
    "failDetails": [
      {"row": 5, "reason": "报名记录不存在"},
      {"row": 12, "reason": "成绩格式错误"},
      {"row": 23, "reason": "该成绩已发布，不可修改"}
    ]
  }
}
​```

---

### 接口40：项目库管理
​```
GET    /api/admin/projects          获取项目库列表
POST   /api/admin/projects          新建自定义项目
PUT    /api/admin/projects/{id}     编辑自定义项目
DELETE /api/admin/projects/{id}     删除自定义项目
权限：SUPER_ADMIN
​```

**删除业务规则**
- category = STANDARD 的项目不可删除，返回 403
- 有赛事正在使用的项目不可删除，返回 409

---

### 接口41：用户管理
​```
GET  /api/admin/users              获取用户列表
PUT  /api/admin/users/{id}/role    修改用户角色
POST /api/admin/users/{id}/disable 禁用用户
POST /api/admin/users/{id}/enable  启用用户
权限：SUPER_ADMIN
​```

**获取用户列表请求参数（Query）**

| 字段 | 类型 | 必填 | 说明 |
|-----|-----|-----|-----|
| role | string | ❌ | 按角色筛选 |
| status | string | ❌ | 按状态筛选 |
| keyword | string | ❌ | 按邮箱或姓名搜索 |
| page | int | ❌ | 默认1 |
| size | int | ❌ | 默认20 |

**修改角色业务规则**
- SUPER_ADMIN 账号角色不可修改，返回 403
- 可设置的角色：USER / EVENT_ADMIN

---

### 接口42：数据统计
​```
GET /api/admin/stats/overview    总览数据
GET /api/admin/stats/events      赛事维度统计
权限：SUPER_ADMIN
```

**总览数据响应**

json

```
{
  "code": 200,
  "data": {
    "totalUsers": 1200,
    "totalEvents": 8,
    "eventsByStatus": {
      "DRAFT": 1,
      "OPEN": 2,
      "CLOSED": 1,
      "ONGOING": 1,
      "FINISHED": 3
    },
    "totalRegistrations": 3560,
    "monthlyNewUsers": 45
  }
}

```

---

## 接口设计总览
```
共 42 个接口，按模块分布：

系统初始化 & 认证    9个   （接口1-9）
公开信息 & 通知      7个   （接口10-16）
运动员申请 & 报名    6个   （接口17-22）
管理员接口          20个   （接口23-42）
```