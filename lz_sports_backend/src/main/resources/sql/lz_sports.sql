/*
 * LZ Sports - full schema (dev reset)
 * Encoding: UTF-8
 * Run: mysql -uroot -p < lz_sports.sql
 * Then: mysql -uroot -p lz_sports < data.sql
 */

/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;

-- 数据库：校园体育赛事库
CREATE DATABASE IF NOT EXISTS `lz_sports` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `lz_sports`;

DROP TABLE IF EXISTS `eligibility_rule`;
DROP TABLE IF EXISTS `eligibility_group`;
DROP TABLE IF EXISTS `eligibility_config`;
DROP TABLE IF EXISTS `score_audit_log`;
DROP TABLE IF EXISTS `result`;
DROP TABLE IF EXISTS `registration`;
DROP TABLE IF EXISTS `sportsimg`;
DROP TABLE IF EXISTS `event_item`;
DROP TABLE IF EXISTS `event_admin_mapping`;
DROP TABLE IF EXISTS `event_status_operation_log`;
DROP TABLE IF EXISTS `athlete`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `admin_user_audit_log`;
DROP TABLE IF EXISTS `event`;
DROP TABLE IF EXISTS `department`;
DROP TABLE IF EXISTS `school_config`;
DROP TABLE IF EXISTS `sys_user`;

/* ==================== sys_user ==================== */
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `password` varchar(100) NOT NULL COMMENT '密码哈希',
  `name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `student_id` varchar(50) DEFAULT NULL COMMENT '学号或工号',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `email` varchar(50) NOT NULL COMMENT '邮箱',
  `contact` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `verify_token` varchar(64) DEFAULT NULL COMMENT '邮箱验证令牌',
  `user_type` varchar(20) NOT NULL COMMENT '角色 SUPER_ADMIN SCHOOL_ADMIN EVENT_ADMIN ATHLETE USER',
  `is_first_login` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否首次登录 0否 1是',
  `unread_count` bigint NOT NULL DEFAULT 0 COMMENT '未读通知数量',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '账号状态 PENDING ACTIVE REJECTED',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `idx_sys_user_verify_token` (`verify_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

/* ==================== school_config ==================== */
CREATE TABLE `school_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_name` varchar(100) NOT NULL COMMENT '学校名称',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo地址',
  `theme_color` varchar(20) DEFAULT '#409EFF' COMMENT '主题色',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `is_initialized` tinyint(1) DEFAULT 0 COMMENT '是否完成初始化 0否 1是',
  `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '组织架构 UNIVERSITY K12',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校配置表';

/* ==================== department ==================== */
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college` varchar(50) DEFAULT NULL COMMENT '学院名称 大学模式',
  `major` varchar(50) DEFAULT NULL COMMENT '专业名称 大学模式',
  `grade` varchar(50) DEFAULT NULL COMMENT '年级名称 K12或大学',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级名称',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '行政部门名称 如教工组',
  `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '组织模式 UNIVERSITY K12',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扁平化组织架构宽表';

/* ==================== event ==================== */
CREATE TABLE `event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '赛事ID',
  `name` varchar(50) NOT NULL COMMENT '赛事名称',
  `description` varchar(500) DEFAULT NULL COMMENT '赛事描述',
  `img_url` varchar(255) DEFAULT NULL COMMENT '封面图URL',
  `reg_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `reg_deadline` datetime DEFAULT NULL COMMENT '报名截止时间',
  `start_time` datetime DEFAULT NULL COMMENT '比赛开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '比赛结束时间',
  `max_items_per_athlete` int DEFAULT 3 COMMENT '每名运动员最多报名项目数',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '赛事状态 DRAFT OPEN CLOSED ONGOING FINISHED',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='赛事活动表';

/* ==================== event_status_operation_log ==================== */
CREATE TABLE `event_status_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `from_status` varchar(20) DEFAULT NULL COMMENT '原状态',
  `to_status` varchar(20) NOT NULL COMMENT '目标状态',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID 定时任务为空',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型 MANUAL AUTO',
  `trigger_source` varchar(20) NOT NULL COMMENT '触发来源 API SCHEDULER',
  `reason` varchar(255) DEFAULT NULL COMMENT '变更原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='赛事状态流转操作日志表';

/* ==================== event_admin_mapping ==================== */
CREATE TABLE `event_admin_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `user_id` bigint NOT NULL COMMENT '赛事管理员用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_user` (`event_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='赛事管理员关联表';

/* ==================== athlete ==================== */
CREATE TABLE `athlete` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `age` varchar(10) DEFAULT NULL COMMENT '年龄字符串',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别 男 女',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `athlete_state` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态 PENDING APPROVED REJECTED',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `agree_time` datetime DEFAULT NULL COMMENT '审核通过时间',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_event` (`user_id`, `event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运动员信息表 按赛事维度';

/* ==================== event_item ==================== */
CREATE TABLE `event_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `event_id` bigint NOT NULL COMMENT '所属赛事ID',
  `name` varchar(50) NOT NULL COMMENT '项目名称',
  `category` varchar(20) DEFAULT NULL COMMENT '项目类别 CUSTOM STANDARD',
  `gender_limit` varchar(20) DEFAULT 'ALL' COMMENT '性别限制 ALL MALE FEMALE 兼容旧字段',
  `limit_dept_ids` varchar(255) DEFAULT NULL COMMENT '部门限制ID集合 JSON 兼容旧字段',
  `max_count` int DEFAULT 20 COMMENT '最大报名人数',
  `current_count` int DEFAULT 0 COMMENT '当前报名人数',
  `start_time` datetime DEFAULT NULL COMMENT '项目开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '项目结束时间',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比赛项目表';

/* ==================== eligibility ==================== */
CREATE TABLE `eligibility_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `owner_type` varchar(32) NOT NULL COMMENT '归属类型 EVENT_ITEM EVENT',
  `owner_id` bigint NOT NULL COMMENT '归属ID 项目ID或赛事ID',
  `group_combination` varchar(8) NOT NULL DEFAULT 'AND' COMMENT '多资格组之间逻辑 AND OR',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用 0禁用等同全员可报 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner` (`owner_type`, `owner_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格规则配置表';

CREATE TABLE `eligibility_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '关联 eligibility_config.id',
  `group_logic` varchar(8) NOT NULL DEFAULT 'AND' COMMENT '组内规则逻辑 AND OR',
  `group_desc` varchar(100) DEFAULT NULL COMMENT '运营可读描述 如大一男生',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格规则组表';

CREATE TABLE `eligibility_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint NOT NULL COMMENT '关联 eligibility_group.id',
  `dimension` varchar(32) NOT NULL COMMENT '维度 GENDER DEPT COLLEGE MAJOR GRADE CLASS AGE',
  `operator` varchar(16) NOT NULL COMMENT '运算符 EQ IN NOT_IN BETWEEN GTE LTE',
  `value_json` text NOT NULL COMMENT '规则值JSON 如男或部门ID数组',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格规则条目表';

/* ==================== notification ==================== */
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '类型 SYSTEM EVENT RESULT',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';

/* ==================== registration ==================== */
CREATE TABLE `registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `item_id` bigint NOT NULL COMMENT '项目ID',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态 PENDING APPROVED REJECTED CONFIRMED CANCELLED',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `registration_time` datetime DEFAULT NULL COMMENT '报名时间',
  `school_id` bigint DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
  KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报名记录表';

/* ==================== result ==================== */
CREATE TABLE `result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `registration_id` bigint NOT NULL COMMENT '报名ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `item_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score_value` varchar(50) DEFAULT NULL COMMENT '成绩值 如10.5s',
  `score_rank` int DEFAULT NULL COMMENT '名次',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_published` tinyint(1) DEFAULT 0 COMMENT '是否发布 0否 1是',
  `published_at` datetime DEFAULT NULL COMMENT '成绩发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registration` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩记录表';

/* ==================== admin_user_audit_log ==================== */
CREATE TABLE `admin_user_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `target_user_id` bigint NOT NULL COMMENT '目标用户ID',
  `action` varchar(64) NOT NULL COMMENT '动作类型',
  `before_role` varchar(20) DEFAULT NULL COMMENT '变更前角色',
  `after_role` varchar(20) DEFAULT NULL COMMENT '变更后角色',
  `before_status` varchar(20) DEFAULT NULL COMMENT '变更前状态',
  `after_status` varchar(20) DEFAULT NULL COMMENT '变更后状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_target_user` (`target_user_id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_action_time` (`action`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理端用户操作审计日志';

/* ==================== score_audit_log ==================== */
CREATE TABLE `score_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `score_id` bigint NOT NULL COMMENT '成绩ID',
  `registration_id` bigint NOT NULL COMMENT '报名ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `item_id` bigint NOT NULL COMMENT '项目ID',
  `athlete_id` bigint NOT NULL COMMENT '运动员用户ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `before_score_value` varchar(50) DEFAULT NULL COMMENT '修改前成绩',
  `before_score_rank` int DEFAULT NULL COMMENT '修改前名次',
  `before_remark` varchar(255) DEFAULT NULL COMMENT '修改前备注',
  `after_score_value` varchar(50) DEFAULT NULL COMMENT '修改后成绩',
  `after_score_rank` int DEFAULT NULL COMMENT '修改后名次',
  `after_remark` varchar(255) DEFAULT NULL COMMENT '修改后备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_score` (`score_id`),
  KEY `idx_registration` (`registration_id`),
  KEY `idx_event_item` (`event_id`, `item_id`),
  KEY `idx_operator_time` (`operator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩变更审计日志';

/* ==================== sportsimg ==================== */
CREATE TABLE `sportsimg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `img_type` varchar(20) NOT NULL COMMENT '图片类型',
  `type_id` bigint NOT NULL COMMENT '关联业务ID',
  `img_src` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片资源表';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
