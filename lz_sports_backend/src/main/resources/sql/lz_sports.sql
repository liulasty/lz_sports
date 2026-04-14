/*
SQLyog Professional v12.09 (64 bit)
MySQL - 8.0.40 : Database - lz_sports
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`lz_sports` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `lz_sports`;

/*Table structure for table `athlete` */

DROP TABLE IF EXISTS `athlete`;

CREATE TABLE `athlete` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `user_id` bigint NOT NULL COMMENT '用户ID',
                           `event_id` bigint NOT NULL COMMENT '赛事ID',
                           `name` varchar(50) NOT NULL COMMENT '姓名',
                           `age` varchar(10) DEFAULT NULL COMMENT '年龄',
                           `gender` varchar(10) DEFAULT NULL COMMENT '性别',
                           `contact` varchar(50) DEFAULT NULL COMMENT '联系方式',
                           `athlete_state` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
                           `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                           `agree_time` datetime DEFAULT NULL COMMENT '审核通过时间',
                           `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_user_event` (`user_id`, `event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运动员信息表';

/*Table structure for table `event` */

DROP TABLE IF EXISTS `event`;

CREATE TABLE `event` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '赛事ID',
                         `name` varchar(50) NOT NULL COMMENT '赛事名称',
                         `description` varchar(500) DEFAULT NULL COMMENT '赛事描述',
                         `img_url` varchar(255) DEFAULT NULL COMMENT '封面图',
                         `reg_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
                         `reg_deadline` datetime DEFAULT NULL COMMENT '报名截止时间',
                         `start_time` datetime DEFAULT NULL COMMENT '比赛开始时间',
                         `end_time` datetime DEFAULT NULL COMMENT '比赛结束时间',
                         `max_items_per_athlete` int DEFAULT '3' COMMENT '每个运动员最多报名项目数',
                         `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/ENDED',
                         `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='赛事活动表';

/*Table structure for table `event_admin_mapping` */

DROP TABLE IF EXISTS `event_admin_mapping`;

CREATE TABLE `event_admin_mapping` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `event_id` bigint NOT NULL COMMENT '赛事ID',
                                       `user_id` bigint NOT NULL COMMENT '用户ID(赛事管理员)',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_event_user` (`event_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='赛事管理员关联表';

/*Table structure for table `event_item` */

DROP TABLE IF EXISTS `event_item`;

CREATE TABLE `event_item` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
                              `event_id` bigint NOT NULL COMMENT '所属赛事ID',
                              `name` varchar(50) NOT NULL COMMENT '项目名称',
                              `category` varchar(20) DEFAULT NULL COMMENT '项目类别',
                              `gender_limit` varchar(20) DEFAULT '无限制' COMMENT '性别限制',
                              `limit_dept_ids` varchar(255) DEFAULT NULL COMMENT '部门限制ID集合(JSON)',
                              `max_count` int DEFAULT '20' COMMENT '最大报名人数',
                              `current_count` int DEFAULT '0' COMMENT '当前报名人数',
                              `start_time` datetime DEFAULT NULL COMMENT '开始时间',
                              `end_time` datetime DEFAULT NULL COMMENT '结束时间',
                              `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比赛项目表';

/*Table structure for table `department` */

DROP TABLE IF EXISTS `department`;

CREATE TABLE `department` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `college` varchar(50) DEFAULT NULL COMMENT '学院名称 (大学模式)',
                         `major` varchar(50) DEFAULT NULL COMMENT '专业名称 (大学模式)',
                         `grade` varchar(50) DEFAULT NULL COMMENT '年级名称 (K12模式)',
                         `class_name` varchar(50) DEFAULT NULL COMMENT '班级名称',
                         `dept_name` varchar(50) DEFAULT NULL COMMENT '行政部门名称 (如体育部/教工组)',
                         `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '组织架构模式: UNIVERSITY/K12',
                         `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                         `sort_order` int DEFAULT '0' COMMENT '排序',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扁平化组织架构宽表';

/*Table structure for table `notification` */

DROP TABLE IF EXISTS `notification`;

CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `user_id` bigint NOT NULL COMMENT '接收用户ID',
                                `title` varchar(100) NOT NULL COMMENT '标题',
                                `content` text COMMENT '内容',
                                `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '类型: SYSTEM/EVENT/RESULT',
                                `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';

/*Table structure for table `registration` */

DROP TABLE IF EXISTS `registration`;

CREATE TABLE `registration` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID (原 AthleteID)',
                                `event_id` bigint NOT NULL COMMENT '赛事ID',
                                `item_id` bigint NOT NULL COMMENT '项目ID',
                                `status` varchar(20) DEFAULT '审核中' COMMENT '状态: 审核中/通过/拒绝',
                                `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
                                `registration_time` datetime DEFAULT NULL COMMENT '报名时间',
                                `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_user_item` (`user_id`,`item_id`),
                                KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报名记录表';

/*Table structure for table `result` */

DROP TABLE IF EXISTS `result`;

CREATE TABLE `result` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `registration_id` bigint NOT NULL COMMENT '报名ID',
                          `event_id` bigint NOT NULL COMMENT '赛事ID',
                          `item_id` bigint NOT NULL COMMENT '项目ID',
                          `user_id` bigint NOT NULL COMMENT '用户ID',
                          `score_value` varchar(50) DEFAULT NULL COMMENT '成绩数值(如 10.5s, 1.8m)',
                          `score_rank` int DEFAULT NULL COMMENT '名次',
                          `is_published` tinyint(1) DEFAULT '0' COMMENT '是否发布: 0-否, 1-是',
                          `published_at` datetime DEFAULT NULL COMMENT '成绩发布时间',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_registration` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩记录表';

/*Table structure for table `school_config` */

DROP TABLE IF EXISTS `school_config`;

CREATE TABLE `school_config` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `school_name` varchar(100) NOT NULL COMMENT '学校名称',
                                 `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo地址',
                                 `theme_color` varchar(20) DEFAULT '#409EFF' COMMENT '主题色',
                                 `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
                                 `is_initialized` tinyint(1) DEFAULT '0' COMMENT '是否已初始化: 0-否, 1-是',
                                 `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '组织架构模式: UNIVERSITY/K12',
                                 `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校配置表';

/*Table structure for table `sportsimg` */

DROP TABLE IF EXISTS `sportsimg`;

CREATE TABLE `sportsimg` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片id',
                             `img_type` varchar(20) NOT NULL COMMENT '图片类型',
                             `type_id` bigint NOT NULL COMMENT '关联ID',
                             `img_src` varchar(255) DEFAULT NULL COMMENT '图片地址',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片资源表';

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `password` varchar(100) NOT NULL COMMENT '密码',
                            `name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
                            `gender` varchar(10) DEFAULT NULL COMMENT '性别',
                            `student_id` varchar(50) DEFAULT NULL COMMENT '学号/工号',
                            `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
                            `email` varchar(50) NOT NULL COMMENT '邮箱',
                            `contact` varchar(20) DEFAULT NULL COMMENT '联系电话',
                            `verify_token` varchar(64) DEFAULT NULL COMMENT '验证令牌',
                            `user_type` varchar(20) NOT NULL COMMENT '用户类型：SUPER_ADMIN/SCHOOL_ADMIN/EVENT_ADMIN/ATHLETE/USER',
                            `is_first_login` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否首次登录：0否1是',
                            `unread_count` bigint NOT NULL DEFAULT '0' COMMENT '未读通知数量',
                            `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ACTIVE/REJECTED',
                            `school_id` bigint DEFAULT '1' COMMENT '学校ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_email` (`email`),
                            UNIQUE KEY `idx_sys_user_verify_token` (`verify_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
