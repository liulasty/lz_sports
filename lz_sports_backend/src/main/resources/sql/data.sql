/*
 * LZ Sports Management System Initial Data Script
 * Version: 2.2
 * Date: 2026-03-06
 * Description: Initial data for Grade, Event, Project, and School Config.
 */

USE `lz_sports`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- 1. Initial School Configuration
-- ----------------------------
INSERT INTO `school_config` (`id`, `school_name`, `logo_url`, `theme_color`, `contact_email`, `is_initialized`, `create_time`) VALUES
(1, 'LZ Sports University', NULL, '#409EFF', 'admin@lzsports.com', 0, NOW());

-- ----------------------------
-- 2. Initial Grades / Departments
-- ----------------------------
INSERT INTO `grade` (`id`, `name`, `school_id`, `sort_order`) VALUES
(1, '计算机科学与技术学院', 1, 1),
(2, '软件学院', 1, 2),
(3, '信息与通信工程学院', 1, 3),
(4, '电子工程学院', 1, 4),
(5, '自动化学院', 1, 5),
(6, '理学院', 1, 6),
(7, '外国语学院', 1, 7),
(8, '人文社科学院', 1, 8),
(9, '经济管理学院', 1, 9),
(10, '体育部', 1, 10),
(11, '教工组', 1, 11),
(12, '研究生院', 1, 12);

-- ----------------------------
-- 3. Initial Events (Demo Data)
-- ----------------------------
-- Event 1: Spring Sports Meeting (Draft)
INSERT INTO `event` (`id`, `name`, `description`, `reg_start_time`, `reg_deadline`, `start_time`, `end_time`, `status`, `school_id`, `create_time`) VALUES
(1, '2026年春季田径运动会', '一年一度的春季运动会，欢迎全校师生踊跃报名！', '2026-03-10 08:00:00', '2026-03-20 18:00:00', '2026-04-01 09:00:00', '2026-04-03 17:00:00', 'DRAFT', 1, NOW());

-- Event 2: Autumn Sports Meeting (Published)
INSERT INTO `event` (`id`, `name`, `description`, `reg_start_time`, `reg_deadline`, `start_time`, `end_time`, `status`, `school_id`, `create_time`) VALUES
(2, '2025年秋季趣味运动会', '趣味项目为主，重在参与，增进友谊。', '2025-09-01 08:00:00', '2025-09-15 18:00:00', '2025-09-25 09:00:00', '2025-09-26 17:00:00', 'PUBLISHED', 1, NOW());

-- ----------------------------
-- 4. Initial Event Items (Projects) for Event 1
-- ----------------------------
INSERT INTO `event_item` (`id`, `event_id`, `name`, `gender_limit`, `grade_limit`, `max_count`, `current_count`, `school_id`, `create_time`) VALUES
(1, 1, '男子100米', '男', NULL, 30, 0, 1, NOW()),
(2, 1, '女子100米', '女', NULL, 30, 0, 1, NOW()),
(3, 1, '男子400米', '男', NULL, 20, 0, 1, NOW()),
(4, 1, '女子400米', '女', NULL, 20, 0, 1, NOW()),
(5, 1, '男子跳远', '男', NULL, 15, 0, 1, NOW()),
(6, 1, '女子跳远', '女', NULL, 15, 0, 1, NOW()),
(7, 1, '男子铅球', '男', NULL, 10, 0, 1, NOW()),
(8, 1, '女子铅球', '女', NULL, 10, 0, 1, NOW()),
(9, 1, '教工男子100米', '男', '11', 20, 0, 1, NOW()),
(10, 1, '教工女子100米', '女', '11', 20, 0, 1, NOW());

-- ----------------------------
-- 5. Initial Event Items (Projects) for Event 2
-- ----------------------------
INSERT INTO `event_item` (`id`, `event_id`, `name`, `gender_limit`, `grade_limit`, `max_count`, `current_count`, `school_id`, `create_time`) VALUES
(11, 2, '二人三足', '无限制', NULL, 50, 0, 1, NOW()),
(12, 2, '袋鼠跳', '无限制', NULL, 50, 0, 1, NOW());

SET FOREIGN_KEY_CHECKS=1;
