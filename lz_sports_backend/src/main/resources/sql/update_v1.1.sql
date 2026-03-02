/*
 * Update: V1.1
 * Date: 2024-03-03
 * Purpose: Add school configuration and multi-school support tables (Phase 1)
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. Create school_config table
-- ----------------------------
DROP TABLE IF EXISTS `school_config`;
CREATE TABLE `school_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_name` varchar(100) NOT NULL COMMENT '学校名称',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo地址',
  `theme_color` varchar(20) DEFAULT '#409EFF' COMMENT '主题色',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校配置表';

-- ----------------------------
-- 2. Create grade table
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
-- 3. Add school_id to existing tables
-- ----------------------------

-- Add to user
ALTER TABLE `user` ADD COLUMN `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID';

-- Add to event
ALTER TABLE `event` ADD COLUMN `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID';
ALTER TABLE `event` ADD COLUMN `status` varchar(20) DEFAULT 'DRAFT' COMMENT '赛事状态: DRAFT, PUBLISHED, ENDED';

-- Add to eventitem
ALTER TABLE `eventitem` ADD COLUMN `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID';

-- Add to registration
ALTER TABLE `registration` ADD COLUMN `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID';

-- Add to athlete
ALTER TABLE `athlete` ADD COLUMN `school_id` bigint(20) DEFAULT 1 COMMENT '学校ID';

SET FOREIGN_KEY_CHECKS = 1;
