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
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                           `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
                           `event_id` bigint NOT NULL COMMENT '璧涗簨ID',
                           `name` varchar(50) NOT NULL COMMENT '濮撳悕',
                           `age` varchar(10) DEFAULT NULL COMMENT '骞撮緞',
                           `gender` varchar(10) DEFAULT NULL COMMENT '鎬у埆',
                           `contact` varchar(50) DEFAULT NULL COMMENT '鑱旂郴鏂瑰紡',
                           `athlete_state` varchar(20) DEFAULT 'PENDING' COMMENT '鐘舵€? PENDING/APPROVED/REJECTED',
                           `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鐢宠鏃堕棿',
                           `agree_time` datetime DEFAULT NULL COMMENT '瀹℃牳閫氳繃鏃堕棿',
                           `dept_id` bigint DEFAULT NULL COMMENT '鎵€灞為儴闂↖D',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_user_event` (`user_id`, `event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='杩愬姩鍛樹俊鎭〃';

/*Table structure for table `event` */

DROP TABLE IF EXISTS `event`;

CREATE TABLE `event` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璧涗簨ID',
                         `name` varchar(50) NOT NULL COMMENT '璧涗簨鍚嶇О',
                         `description` varchar(500) DEFAULT NULL COMMENT '璧涗簨鎻忚堪',
                         `img_url` varchar(255) DEFAULT NULL COMMENT '灏侀潰鍥?,
                         `reg_start_time` datetime DEFAULT NULL COMMENT '鎶ュ悕寮€濮嬫椂闂?,
                         `reg_deadline` datetime DEFAULT NULL COMMENT '鎶ュ悕鎴鏃堕棿',
                         `start_time` datetime DEFAULT NULL COMMENT '姣旇禌寮€濮嬫椂闂?,
                         `end_time` datetime DEFAULT NULL COMMENT '姣旇禌缁撴潫鏃堕棿',
                         `max_items_per_athlete` int DEFAULT '3' COMMENT '姣忎釜杩愬姩鍛樻渶澶氭姤鍚嶉」鐩暟',
                         `status` varchar(20) DEFAULT 'DRAFT' COMMENT '鐘舵€? DRAFT/PUBLISHED/ENDED',
                         `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璧涗簨娲诲姩琛?;

/*Table structure for table `event_admin_mapping` */

DROP TABLE IF EXISTS `event_admin_mapping`;

CREATE TABLE `event_admin_mapping` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                                       `event_id` bigint NOT NULL COMMENT '璧涗簨ID',
                                       `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID(璧涗簨绠＄悊鍛?',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_event_user` (`event_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璧涗簨绠＄悊鍛樺叧鑱旇〃';

/*Table structure for table `event_item` */

DROP TABLE IF EXISTS `event_item`;

CREATE TABLE `event_item` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '椤圭洰ID',
                              `event_id` bigint NOT NULL COMMENT '鎵€灞炶禌浜婭D',
                              `name` varchar(50) NOT NULL COMMENT '椤圭洰鍚嶇О',
                              `category` varchar(20) DEFAULT NULL COMMENT '椤圭洰绫诲埆',
                              `gender_limit` varchar(20) DEFAULT '鏃犻檺鍒? COMMENT '鎬у埆闄愬埗',
                              `limit_dept_ids` varchar(255) DEFAULT NULL COMMENT '閮ㄩ棬闄愬埗ID闆嗗悎(JSON)',
                              `max_count` int DEFAULT '20' COMMENT '鏈€澶ф姤鍚嶄汉鏁?,
                              `current_count` int DEFAULT '0' COMMENT '褰撳墠鎶ュ悕浜烘暟',
                              `start_time` datetime DEFAULT NULL COMMENT '寮€濮嬫椂闂?,
                              `end_time` datetime DEFAULT NULL COMMENT '缁撴潫鏃堕棿',
                              `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                              PRIMARY KEY (`id`),
                              KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='姣旇禌椤圭洰琛?;

/*Table structure for table `department` */

DROP TABLE IF EXISTS `department`;

CREATE TABLE `department` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                         `college` varchar(50) DEFAULT NULL COMMENT '瀛﹂櫌鍚嶇О (澶у妯″紡)',
                         `major` varchar(50) DEFAULT NULL COMMENT '涓撲笟鍚嶇О (澶у妯″紡)',
                         `grade` varchar(50) DEFAULT NULL COMMENT '骞寸骇鍚嶇О (K12妯″紡)',
                         `class_name` varchar(50) DEFAULT NULL COMMENT '鐝骇鍚嶇О',
                         `dept_name` varchar(50) DEFAULT NULL COMMENT '琛屾斂閮ㄩ棬鍚嶇О (濡備綋鑲查儴/鏁欏伐缁?',
                         `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '缁勭粐鏋舵瀯妯″紡: UNIVERSITY/K12',
                         `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                         `sort_order` int DEFAULT '0' COMMENT '鎺掑簭',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎵佸钩鍖栫粍缁囨灦鏋勫琛?;

/*Table structure for table `notification` */

DROP TABLE IF EXISTS `notification`;

CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                                `user_id` bigint NOT NULL COMMENT '鎺ユ敹鐢ㄦ埛ID',
                                `title` varchar(100) NOT NULL COMMENT '鏍囬',
                                `content` text COMMENT '鍐呭',
                                `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '绫诲瀷: SYSTEM/EVENT/RESULT',
                                `is_read` tinyint(1) DEFAULT '0' COMMENT '鏄惁宸茶: 0-鏈, 1-宸茶',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绯荤粺閫氱煡琛?;

/*Table structure for table `registration` */

DROP TABLE IF EXISTS `registration`;

CREATE TABLE `registration` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鎶ュ悕ID',
                                `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID (鍘?AthleteID)',
                                `event_id` bigint NOT NULL COMMENT '璧涗簨ID',
                                `item_id` bigint NOT NULL COMMENT '椤圭洰ID',
                                `status` varchar(20) DEFAULT '瀹℃牳涓? COMMENT '鐘舵€? 瀹℃牳涓?閫氳繃/鎷掔粷',
                                `reject_reason` varchar(255) DEFAULT NULL COMMENT '鎷掔粷鍘熷洜',
                                `registration_time` datetime DEFAULT NULL COMMENT '鎶ュ悕鏃堕棿',
                                `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_user_item` (`user_id`,`item_id`),
                                KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎶ュ悕璁板綍琛?;

/*Table structure for table `result` */

DROP TABLE IF EXISTS `result`;

CREATE TABLE `result` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                          `registration_id` bigint NOT NULL COMMENT '鎶ュ悕ID',
                          `event_id` bigint NOT NULL COMMENT '璧涗簨ID',
                          `item_id` bigint NOT NULL COMMENT '椤圭洰ID',
                          `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
                          `score_value` varchar(50) DEFAULT NULL COMMENT '鎴愮哗鏁板€?濡?10.5s, 1.8m)',
                          `score_rank` int DEFAULT NULL COMMENT '鍚嶆',
                          `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
                          `is_published` tinyint(1) DEFAULT '0' COMMENT '鏄惁鍙戝竷: 0-鍚? 1-鏄?,
                          `published_at` datetime DEFAULT NULL COMMENT '鎴愮哗鍙戝竷鏃堕棿',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '褰曞叆鏃堕棿',
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_registration` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎴愮哗璁板綍琛?;

/*Table structure for table `school_config` */

DROP TABLE IF EXISTS `school_config`;

CREATE TABLE `school_config` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                                 `school_name` varchar(100) NOT NULL COMMENT '瀛︽牎鍚嶇О',
                                 `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo鍦板潃',
                                 `theme_color` varchar(20) DEFAULT '#409EFF' COMMENT '涓婚鑹?,
                                 `contact_email` varchar(100) DEFAULT NULL COMMENT '鑱旂郴閭',
                                 `is_initialized` tinyint(1) DEFAULT '0' COMMENT '鏄惁宸插垵濮嬪寲: 0-鍚? 1-鏄?,
                                 `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '缁勭粐鏋舵瀯妯″紡: UNIVERSITY/K12',
                                 `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='瀛︽牎閰嶇疆琛?;

/*Table structure for table `sportsimg` */

DROP TABLE IF EXISTS `sportsimg`;

CREATE TABLE `sportsimg` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍥剧墖id',
                             `img_type` varchar(20) NOT NULL COMMENT '鍥剧墖绫诲瀷',
                             `type_id` bigint NOT NULL COMMENT '鍏宠仈ID',
                             `img_src` varchar(255) DEFAULT NULL COMMENT '鍥剧墖鍦板潃',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鍥剧墖璧勬簮琛?;

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
                            `username` varchar(50) NOT NULL COMMENT '鐢ㄦ埛鍚?,
                            `password` varchar(100) NOT NULL COMMENT '瀵嗙爜',
                            `name` varchar(50) DEFAULT NULL COMMENT '鐪熷疄濮撳悕',
                            `gender` varchar(10) DEFAULT NULL COMMENT '鎬у埆',
                            `student_id` varchar(50) DEFAULT NULL COMMENT '瀛﹀彿/宸ュ彿',
                            `dept_id` bigint DEFAULT NULL COMMENT '鎵€灞為儴闂↖D',
                            `email` varchar(50) NOT NULL COMMENT '閭',
                            `contact` varchar(20) DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽',
                            `verify_token` varchar(64) DEFAULT NULL COMMENT '楠岃瘉浠ょ墝',
                            `user_type` varchar(20) NOT NULL COMMENT '鐢ㄦ埛绫诲瀷锛歋UPER_ADMIN/SCHOOL_ADMIN/EVENT_ADMIN/ATHLETE/USER',
                            `is_first_login` tinyint(1) NOT NULL DEFAULT '0' COMMENT '鏄惁棣栨鐧诲綍锛?鍚?鏄?,
                            `unread_count` bigint NOT NULL DEFAULT '0' COMMENT '鏈閫氱煡鏁伴噺',
                            `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '鐘舵€侊細PENDING/ACTIVE/REJECTED',
                            `school_id` bigint DEFAULT '1' COMMENT '瀛︽牎ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '娉ㄥ唽鏃堕棿',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_email` (`email`),
                            UNIQUE KEY `idx_sys_user_verify_token` (`verify_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绯荤粺鐢ㄦ埛琛?;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
