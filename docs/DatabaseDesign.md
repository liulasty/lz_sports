## Step 3：数据库设计

内容较多，我分三批输出，每批确认后再输出下一批。

------

### 3.1 ER关系总览

```
school_config（1）
    │
    └── 全局唯一，所有数据归属于此学校

sys_user（1）─────────────────────────────（N）notification
    │
    ├──（1:N）athlete_registration（运动员申请）
    │              │
    │              └──（属于）event（赛事）
    │
    └──（1:N）event_item_registration（项目报名）
                   │
                   └──（属于）event_item（比赛项目）

event（1）
    ├──（1:N）event_item（比赛项目）
    │              └──（1:N）event_item_registration
    ├──（1:N）athlete_registration（运动员申请）
    ├──（M:N）sys_user（赛事管理员）→ event_admin_mapping
    └──（1:N）result（成绩）

project_library（项目库）
    └──（1:N）event_item（赛事引用项目库）
```

------

### 3.2 第一批：基础表（共4张）

------

#### 表1：school_config（学校配置表）

说明：全库唯一一条记录，存储学校个性化配置。

| 字段名            | 类型       | 长度   | 可空   | 默认值  | 说明       |
| -------------- | -------- | ---- | ---- | ---- | -------- |
| id             | bigint   | -    | NO   | auto | 主键       |
| school_name    | varchar  | 100  | NO   | -    | 学校名称     |
| logo_url       | varchar  | 500  | YES  | NULL | Logo图片路径 |
| theme_code     | varchar  | 20   | NO   | BLUE | 主题色代码    |
| is_initialized | tinyint  | 1    | NO   | 0    | 是否已完成初始化 |
| created_at     | datetime | -    | NO   | -    | 创建时间     |
| updated_at     | datetime | -    | NO   | -    | 更新时间     |

**主题色枚举值**：BLUE / GREEN / RED / PURPLE / ORANGE / GRAY

**索引**：PRIMARY KEY (id)

------

#### 表2：sys_user（用户表）

说明：所有用户统一存储，角色通过role字段区分。

| 字段名            | 类型       | 长度   | 可空   | 默认值    | 说明                                  |
| -------------- | -------- | ---- | ---- | ------ | ----------------------------------- |
| id             | bigint   | -    | NO   | auto   | 主键                                  |
| email          | varchar  | 100  | NO   | -      | 登录邮箱，唯一                             |
| password       | varchar  | 255  | NO   | -      | BCrypt加密密码                          |
| role           | varchar  | 20   | NO   | USER   | 角色：SUPER_ADMIN / EVENT_ADMIN / USER |
| status         | varchar  | 20   | NO   | ACTIVE | 状态：ACTIVE / DISABLED                |
| real_name      | varchar  | 50   | YES  | NULL   | 真实姓名（申请运动员时填写）                      |
| department     | varchar  | 100  | YES  | NULL   | 院系                                  |
| grade          | varchar  | 20   | YES  | NULL   | 年级                                  |
| phone          | varchar  | 20   | YES  | NULL   | 联系方式                                |
| is_first_login | tinyint  | 1    | NO   | 0      | 是否首次登录（超管强制改密码用）                    |
| unread_count   | int      | -    | NO   | 0      | 未读消息数量（冗余字段，提升查询性能）                 |
| created_at     | datetime | -    | NO   | -      | 创建时间                                |
| updated_at     | datetime | -    | NO   | -      | 更新时间                                |

**索引**：

- PRIMARY KEY (id)
- UNIQUE KEY uk_email (email)
- INDEX idx_role (role)
- INDEX idx_status (status)

------

#### 表3：project_library（项目库表）

说明：全校共用的项目资源池，赛事从这里选取项目。

| 字段名         | 类型       | 长度   | 可空   | 默认值        | 说明                             |
| ----------- | -------- | ---- | ---- | ---------- | ------------------------------ |
| id          | bigint   | -    | NO   | auto       | 主键                             |
| name        | varchar  | 100  | NO   | -          | 项目名称                           |
| category    | varchar  | 20   | NO   | CUSTOM     | 类别：STANDARD（内置）/ CUSTOM（自定义）   |
| type        | varchar  | 20   | NO   | INDIVIDUAL | 类型：INDIVIDUAL（个人）/ TEAM（团队，预留） |
| description | varchar  | 500  | YES  | NULL       | 项目描述                           |
| is_enabled  | tinyint  | 1    | NO   | 1          | 是否启用                           |
| created_by  | bigint   | -    | YES  | NULL       | 创建人id（内置项目为NULL）               |
| created_at  | datetime | -    | NO   | -          | 创建时间                           |
| updated_at  | datetime | -    | NO   | -          | 更新时间                           |

**关键规则**：category = STANDARD 的记录不可删除，由程序层拦截。

**索引**：

- PRIMARY KEY (id)
- INDEX idx_category (category)
- INDEX idx_enabled (is_enabled)

**初始化内置数据**：

```
100米、200米、400米、800米、1500米
跳远、三级跳远、跳高
铅球、实心球
乒乓球（单打）、羽毛球（单打）、篮球（三对三）
```

------

#### 表4：notification（站内信表）

说明：系统通知，支持已读/未读状态。

| 字段名        | 类型       | 长度   | 可空   | 默认值  | 说明                                       |
| ---------- | -------- | ---- | ---- | ---- | ---------------------------------------- |
| id         | bigint   | -    | NO   | auto | 主键                                       |
| user_id    | bigint   | -    | NO   | -    | 接收用户id                                   |
| title      | varchar  | 100  | NO   | -    | 通知标题                                     |
| content    | varchar  | 500  | NO   | -    | 通知内容                                     |
| type       | varchar  | 30   | NO   | -    | 类型：ATHLETE_APPROVED / ATHLETE_REJECTED / RESULT_PUBLISHED / EVENT_PUBLISHED |
| related_id | bigint   | -    | YES  | NULL | 关联业务id（赛事id或成绩id）                        |
| is_read    | tinyint  | 1    | NO   | 0    | 是否已读                                     |
| created_at | datetime | -    | NO   | -    | 创建时间                                     |

**索引**：

- PRIMARY KEY (id)
- INDEX idx_user_read (user_id, is_read)（高频查询：某用户的未读消息）
- INDEX idx_user_id (user_id)

------

### 3.3 第二批：赛事核心表（共3张）

------

#### 表5：event（赛事表）

说明：一场运动会的主体信息。

| 字段名                   | 类型       | 长度   | 可空   | 默认值   | 说明                                    |
| --------------------- | -------- | ---- | ---- | ----- | ------------------------------------- |
| id                    | bigint   | -    | NO   | auto  | 主键                                    |
| name                  | varchar  | 100  | NO   | -     | 赛事名称                                  |
| description           | text     | -    | YES  | NULL  | 赛事描述                                  |
| cover_url             | varchar  | 500  | YES  | NULL  | 封面图路径                                 |
| venue                 | varchar  | 200  | YES  | NULL  | 举办地点                                  |
| status                | varchar  | 20   | NO   | DRAFT | 状态：DRAFT/OPEN/CLOSED/ONGOING/FINISHED |
| reg_start_time        | datetime | -    | NO   | -     | 报名开始时间                                |
| reg_end_time          | datetime | -    | NO   | -     | 报名截止时间                                |
| event_start_time      | datetime | -    | NO   | -     | 比赛开始时间                                |
| event_end_time        | datetime | -    | NO   | -     | 比赛结束时间                                |
| max_items_per_athlete | int      | -    | NO   | 3     | 每位运动员最多报名项目数                          |
| created_by            | bigint   | -    | NO   | -     | 创建人id（超级管理员）                          |
| created_at            | datetime | -    | NO   | -     | 创建时间                                  |
| updated_at            | datetime | -    | NO   | -     | 更新时间                                  |

**关键规则**：

- status = OPEN 后，max_items_per_athlete 不可修改
- reg_end_time 必须 > reg_start_time
- event_start_time 必须 > reg_end_time

**索引**：

- PRIMARY KEY (id)
- INDEX idx_status (status)
- INDEX idx_reg_time (reg_start_time, reg_end_time)

------

#### 表6：event_item（赛事项目表）

说明：某个赛事下的具体比赛项目，从项目库引用。

| 字段名              | 类型       | 长度   | 可空   | 默认值   | 说明                             |
| ---------------- | -------- | ---- | ---- | ----- | ------------------------------ |
| id               | bigint   | -    | NO   | auto  | 主键                             |
| event_id         | bigint   | -    | NO   | -     | 所属赛事id                         |
| project_id       | bigint   | -    | NO   | -     | 引用项目库id                        |
| name             | varchar  | 100  | NO   | -     | 项目名称（冗余，避免联表）                  |
| item_start_time  | datetime | -    | YES  | NULL  | 该项目比赛开始时间                      |
| item_end_time    | datetime | -    | YES  | NULL  | 该项目比赛结束时间                      |
| max_participants | int      | -    | NO   | 100   | 参赛人数上限                         |
| current_count    | int      | -    | NO   | 0     | 当前报名人数（原子更新，防超卖）               |
| gender_limit     | varchar  | 10   | NO   | ALL   | 性别限制：ALL/MALE/FEMALE           |
| description      | varchar  | 500  | YES  | NULL  | 项目补充说明                         |
| round            | varchar  | 20   | NO   | FINAL | 预留：PRELIMINARY/SEMIFINAL/FINAL |
| created_at       | datetime | -    | NO   | -     | 创建时间                           |
| updated_at       | datetime | -    | NO   | -     | 更新时间                           |

**关键规则**：

- item_start_time 和 item_end_time 必须在所属 event 的时间范围内
- 时间冲突校验只在同一 event_id 下的项目间进行

**索引**：

- PRIMARY KEY (id)
- INDEX idx_event_id (event_id)
- INDEX idx_project_id (project_id)

------

#### 表7：event_admin_mapping（赛事管理员绑定表）

说明：赛事与管理员的多对多关系。

| 字段名        | 类型       | 长度   | 可空   | 默认值  | 说明      |
| ---------- | -------- | ---- | ---- | ---- | ------- |
| id         | bigint   | -    | NO   | auto | 主键      |
| event_id   | bigint   | -    | NO   | -    | 赛事id    |
| user_id    | bigint   | -    | NO   | -    | 管理员用户id |
| created_at | datetime | -    | NO   | -    | 绑定时间    |

**索引**：

- PRIMARY KEY (id)
- UNIQUE KEY uk_event_user (event_id, user_id)
- INDEX idx_user_id (user_id)（查某人管理的所有赛事）

------

### 3.4 第三批：报名与成绩表（共2张）

------

#### 表8：athlete_registration（运动员申请表）

说明：用户申请成为某赛事运动员的记录，身份归属于赛事。

| 字段名           | 类型       | 长度   | 可空   | 默认值     | 说明                           |
| ------------- | -------- | ---- | ---- | ------- | ---------------------------- |
| id            | bigint   | -    | NO   | auto    | 主键                           |
| user_id       | bigint   | -    | NO   | -       | 申请用户id                       |
| event_id      | bigint   | -    | NO   | -       | 申请的赛事id                      |
| real_name     | varchar  | 50   | NO   | -       | 真实姓名（本次申请使用）                 |
| department    | varchar  | 100  | YES  | NULL    | 院系                           |
| grade         | varchar  | 20   | YES  | NULL    | 年级                           |
| phone         | varchar  | 20   | YES  | NULL    | 联系方式                         |
| status        | varchar  | 20   | NO   | PENDING | 状态：PENDING/APPROVED/REJECTED |
| reject_reason | varchar  | 500  | YES  | NULL    | 拒绝原因                         |
| reviewed_by   | bigint   | -    | YES  | NULL    | 审核人id                        |
| reviewed_at   | datetime | -    | YES  | NULL    | 审核时间                         |
| created_at    | datetime | -    | NO   | -       | 申请时间                         |
| updated_at    | datetime | -    | NO   | -       | 更新时间                         |

**索引**：

- PRIMARY KEY (id)
- UNIQUE KEY uk_user_event (user_id, event_id)（同一用户同一赛事只能有一条申请）
- INDEX idx_event_status (event_id, status)（管理员查某赛事待审核列表）
- INDEX idx_user_id (user_id)

------

#### 表9：item_registration（项目报名表）

说明：运动员报名某赛事具体项目的记录。

| 字段名           | 类型       | 长度   | 可空   | 默认值       | 说明                     |
| ------------- | -------- | ---- | ---- | --------- | ---------------------- |
| id            | bigint   | -    | NO   | auto      | 主键                     |
| user_id       | bigint   | -    | NO   | -         | 运动员用户id                |
| event_id      | bigint   | -    | NO   | -         | 赛事id（冗余，方便查询）          |
| event_item_id | bigint   | -    | NO   | -         | 报名的赛事项目id              |
| status        | varchar  | 20   | NO   | CONFIRMED | 状态：CONFIRMED/CANCELLED |
| cancelled_at  | datetime | -    | YES  | NULL      | 取消时间                   |
| created_at    | datetime | -    | NO   | -         | 报名时间                   |
| updated_at    | datetime | -    | NO   | -         | 更新时间                   |

**索引**：

- PRIMARY KEY (id)
- UNIQUE KEY uk_user_item (user_id, event_item_id)（同一用户同一项目不可重复报名）
- INDEX idx_event_item (event_id, event_item_id)
- INDEX idx_user_event (user_id, event_id)（查某用户在某赛事的所有报名）

------

#### 表10：result（成绩表）

说明：赛事项目的最终成绩，支持多轮字段预留。

| 字段名                  | 类型       | 长度   | 可空   | 默认值   | 说明                               |
| -------------------- | -------- | ---- | ---- | ----- | -------------------------------- |
| id                   | bigint   | -    | NO   | auto  | 主键                               |
| event_id             | bigint   | -    | NO   | -     | 赛事id（冗余）                         |
| event_item_id        | bigint   | -    | NO   | -     | 赛事项目id                           |
| user_id              | bigint   | -    | NO   | -     | 运动员id                            |
| item_registration_id | bigint   | -    | NO   | -     | 关联报名记录id                         |
| score                | varchar  | 100  | YES  | NULL  | 最终成绩（varchar兼容多种格式）              |
| rank                 | int      | -    | YES  | NULL  | 最终排名                             |
| remarks              | varchar  | 500  | YES  | NULL  | 备注                               |
| round                | varchar  | 20   | NO   | FINAL | 预留轮次：PRELIMINARY/SEMIFINAL/FINAL |
| preliminary_score    | varchar  | 100  | YES  | NULL  | 预留：预赛成绩                          |
| semifinal_score      | varchar  | 100  | YES  | NULL  | 预留：半决赛成绩                         |
| is_published         | tinyint  | 1    | NO   | 0     | 是否已发布（0草稿/1发布）                   |
| published_at         | datetime | -    | YES  | NULL  | 发布时间                             |
| created_by           | bigint   | -    | NO   | -     | 录入人id                            |
| created_at           | datetime | -    | NO   | -     | 创建时间                             |
| updated_at           | datetime | -    | NO   | -     | 更新时间                             |

**关键规则**：

- is_published = 1 后，score 和 rank 字段不可修改，程序层拦截
- UNIQUE KEY 保证一条报名记录只有一条成绩

**索引**：

- PRIMARY KEY (id)
- UNIQUE KEY uk_registration (item_registration_id)
- INDEX idx_event_item (event_id, event_item_id)
- INDEX idx_published (is_published)
- INDEX idx_user_id (user_id)

------

### 3.5 数据库设计总览

```
共 10 张表：

基础层（4张）
├── school_config      学校配置
├── sys_user           用户
├── project_library    项目库
└── notification       站内信

赛事层（3张）
├── event              赛事
├── event_item         赛事项目
└── event_admin_mapping 赛事管理员绑定

业务层（3张）
├── athlete_registration  运动员申请
├── item_registration     项目报名
└── result                成绩
```