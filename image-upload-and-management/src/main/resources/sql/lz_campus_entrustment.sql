-- 创建用户表
CREATE TABLE Users
(
    -- 用户ID：自增长的整数，作为主键
    UserID   INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',

    -- 用户名：非空
    Username VARCHAR(255)           NOT NULL COMMENT '用户名',

    -- 密码：非空
    PASSWORD VARCHAR(255)           NOT NULL COMMENT '密码',

    -- 电子邮件地址：唯一且非空
    Email    VARCHAR(255)           NOT NULL COMMENT '电子邮件地址',


    -- 用户是否激活：默认为TRUE
    IsActive BOOLEAN                NOT NULL DEFAULT TRUE COMMENT '用户是否激活',

    -- 用户角色：默认为'user'
    Role     ENUM ('user', 'admin') NOT NULL DEFAULT 'user' COMMENT '用户角色'
) COMMENT ='存储系统用户信息';

-- 创建用户信息表
CREATE TABLE UsersInfo
(
    -- 用户ID：自增长的整数，作为主键
    UserID      INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',

    -- 用户名：非空
    NAME        VARCHAR(255)                        NOT NULL COMMENT '姓名',

    -- 电话号码
    PhoneNumber VARCHAR(15) COMMENT '电话号码',
    -- qq
    qqNumber    VARCHAR(15) COMMENT '号码',

    -- 认证图片
    roleImgSrc  VARCHAR(15) COMMENT '认证图片地址',

    -- 用户是否激活：默认为TRUE
    IsActive    BOOLEAN                             NOT NULL DEFAULT TRUE COMMENT '用户是否激活',

    -- 用户角色：默认为'user'
    userRole    ENUM ('student', 'teacher','other') NOT NULL COMMENT '用户角色'
) COMMENT ='存储系统用户详细信息';


-- 创建任务表
CREATE TABLE Task
(
    -- 任务ID：自增长的整数，作为主键
    TaskID      INT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',

    -- 任务所有者的用户ID
    OwnerID     INT COMMENT '任务所有者的用户ID',

    -- 任务接收者的用户ID
    ReceiverID  INT COMMENT '任务接收者的用户ID',

    -- 任务描述：非空
    Description VARCHAR(255)                                        NOT NULL COMMENT '任务描述',

    -- 任务地点
    Location    VARCHAR(255) COMMENT '任务地点',

    -- 任务开始时间
    StartTime   DATETIME COMMENT '任务开始时间',

    -- 任务结束时间
    EndTime     DATETIME COMMENT '任务结束时间',

    -- 任务状态：默认为'pending'
    STATUS      ENUM ('Auditing','pending', 'Ongoing', 'completed') NOT NULL DEFAULT 'pending' COMMENT '任务状态'
) COMMENT ='存储任务相关信息';

-- 创建任务更新记录表
CREATE TABLE TaskUpdates
(
    -- 更新记录的ID：自增长的整数，作为主键
    UpdateID          INT AUTO_INCREMENT PRIMARY KEY COMMENT '更新记录的ID',

    -- 对应的任务ID
    TaskID            INT COMMENT '对应的任务ID',

    -- 进行更新的用户ID
    UserID            INT COMMENT '进行更新的用户ID',

    -- 更新时间：默认为当前时间
    UpdateTime        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 更新描述：非空
    UpdateDescription TEXT NOT NULL COMMENT '更新描述'
) COMMENT ='记录任务的更新情况';

-- 创建评价表
CREATE TABLE Reviews
(
    -- 评价ID：自增长的整数，作为主键
    ReviewID   INT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',

    -- 对应的任务ID
    TaskID     INT COMMENT '对应的任务ID',

    -- 评价者的用户ID
    ReviewerID INT COMMENT '评价者的用户ID',

    -- 评价等级（1到5之间）
    Rating     INT CHECK (Rating >= 1 AND Rating <= 5) COMMENT '评价等级',

    -- 评价评论
    COMMENT    VARCHAR(255) COMMENT '评价评论',

    -- 是否已批准：默认为FALSE
    IsApproved BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已批准'
) COMMENT ='存储用户对任务的评价信息';

-- 创建通知表
CREATE TABLE Notifications
(
    -- 通知ID：自增长的整数，作为主键
    NotificationID   INT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',

    -- 接收通知的用户ID
    UserID           INT COMMENT '接收通知的用户ID',

    -- 通知类型
    NotificationType ENUM ('task_accepted', 'task_completed', 'new_review') NOT NULL COMMENT '通知类型',

    -- 通知消息：非空
    Message          VARCHAR(255)                                           NOT NULL COMMENT '通知消息',

    -- 是否已读：默认为FALSE
    IsRead           BOOLEAN                                                NOT NULL DEFAULT FALSE COMMENT '是否已读',

    -- 通知时间：默认为当前时间
    NotificationTime DATETIME                                                        DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间'
) COMMENT ='存储系统通知信息';

-- 创建管理员设置表
CREATE TABLE AdminSettings
(
    -- 设置ID：自增长的整数，作为主键
    SettingID    INT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',

    -- 设置键：唯一且非空
    SettingKey   VARCHAR(255) UNIQUE NOT NULL COMMENT '设置键',

    -- 设置值：非空
    SettingValue VARCHAR(255)        NOT NULL COMMENT '设置值'
) COMMENT ='存储系统管理员相关设置';