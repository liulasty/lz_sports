/*
 * LZ Sports Management System Database Initialization Script
 * Version: 2.1 (Refactored based on PRD Transformation Goals)
 * Date: 2026-03-03
 * Description: Contains all tables for School Admin, Event Admin, Athlete, and Result Management.
 */

CREATE DATABASE IF NOT EXISTS `lz_sports` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `lz_sports`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- 1. School Configuration (Global)
-- ----------------------------
DROP TABLE IF EXISTS `school_config`;
CREATE TABLE `school_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '学校名称',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo地址',
  `theme_color` varchar(20) DEFAULT '#409EFF' COMMENT '主题色',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校配置表';

-- ----------------------------
-- 2. Grade/Department Dictionary
-- ----------------------------
DROP TABLE IF EXISTS `grade`;
CREATE TABLE `grade` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '年级名称',
  `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级/院系表';

-- ----------------------------
-- 3. System User (Merged with Athlete)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `student_id` varchar(50) DEFAULT NULL COMMENT '学号/工号',
  `grade_id` bigint(20) DEFAULT NULL COMMENT '所属年级ID',
  `email` varchar(50) NOT NULL COMMENT '邮箱',
  `role` varchar(20) NOT NULL COMMENT '角色：SCHOOL_ADMIN/EVENT_ADMIN/ATHLETE',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ACTIVE/REJECTED',
  `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 4. Event (Sports Meeting)
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '赛事ID',
  `name` varchar(50) NOT NULL COMMENT '赛事名称',
  `description` varchar(500) DEFAULT NULL COMMENT '赛事描述',
  `img_url` varchar(255) DEFAULT NULL COMMENT '封面图',
  `reg_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `reg_deadline` datetime DEFAULT NULL COMMENT '报名截止时间',
  `start_time` datetime DEFAULT NULL COMMENT '比赛开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '比赛结束时间',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/ENDED',
  `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事活动表';

-- ----------------------------
-- 5. Event Item (Projects)
-- ----------------------------
DROP TABLE IF EXISTS `event_item`;
CREATE TABLE `event_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `event_id` bigint(20) NOT NULL COMMENT '所属赛事ID',
  `name` varchar(50) NOT NULL COMMENT '项目名称',
  `gender_limit` varchar(20) DEFAULT '无限制' COMMENT '性别限制',
  `grade_limit` varchar(50) DEFAULT NULL COMMENT '年级限制',
  `max_count` int(11) DEFAULT 20 COMMENT '最大报名人数',
  `current_count` int(11) DEFAULT 0 COMMENT '当前报名人数',
  `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='比赛项目表';

-- ----------------------------
-- 6. Event Admin Mapping
-- ----------------------------
DROP TABLE IF EXISTS `event_admin_mapping`;
CREATE TABLE `event_admin_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint(20) NOT NULL COMMENT '赛事ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID(赛事管理员)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_user` (`event_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事管理员关联表';

-- ----------------------------
-- 7. Registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID (原 AthleteID)',
  `event_id` bigint(20) NOT NULL COMMENT '赛事ID',
  `item_id` bigint(20) NOT NULL COMMENT '项目ID',
  `status` varchar(20) DEFAULT '审核中' COMMENT '状态: 审核中/通过/拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
  KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';

-- ----------------------------
-- 8. Result (Score)
-- ----------------------------
DROP TABLE IF EXISTS `result`;
CREATE TABLE `result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `registration_id` bigint(20) NOT NULL COMMENT '报名ID',
  `event_id` bigint(20) NOT NULL COMMENT '赛事ID',
  `item_id` bigint(20) NOT NULL COMMENT '项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `score_value` varchar(50) DEFAULT NULL COMMENT '成绩数值(如 10.5s, 1.8m)',
  `score_rank` int(11) DEFAULT NULL COMMENT '名次',
  `is_published` tinyint(1) DEFAULT 0 COMMENT '是否发布: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registration` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩记录表';

-- ----------------------------
-- 9. Images (OSS/Local) - Optional but kept for compatibility
-- ----------------------------
DROP TABLE IF EXISTS `sportsimg`;
CREATE TABLE `sportsimg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片id',
  `type` varchar(20) NOT NULL COMMENT '图片类型',
  `type_id` bigint(20) NOT NULL COMMENT '关联ID',
  `url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片资源表';

SET FOREIGN_KEY_CHECKS=1;
