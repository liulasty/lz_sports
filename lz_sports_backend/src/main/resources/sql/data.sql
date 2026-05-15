/*
 * LZ Sports Management System Initial Data Script
 * Version: 2.3
 * Date: 2026-05-15
 * Description: Dev seed data. Run after lz_sports.sql (full schema + eligibility tables).
 * gender_limit: ALL | MALE | FEMALE (matches GenderLimit enum).
 */

USE `lz_sports`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- 1. Initial School Configuration
-- ----------------------------
INSERT INTO `school_config` (`id`, `school_name`, `logo_url`, `theme_color`, `contact_email`, `is_initialized`, `org_mode`, `create_time`) VALUES
(1, 'LZ Sports University', NULL, '#409EFF', 'admin@lzsports.com', 0, 'UNIVERSITY', NOW());

-- ----------------------------
-- 2. Initial Department / Organization (Flat Table Demo)
-- ----------------------------
INSERT INTO `department` (`id`, `college`, `major`, `grade`, `class_name`, `dept_name`, `org_mode`, `school_id`, `sort_order`) VALUES
(1, '计算机科学与技术学院', '软件工程', NULL, '软工1班', NULL, 'UNIVERSITY', 1, 1),
(2, '计算机科学与技术学院', '软件工程', NULL, '软工2班', NULL, 'UNIVERSITY', 1, 2),
(3, '电子工程学院', '通信工程', NULL, '通信1班', NULL, 'UNIVERSITY', 1, 3),
(4, NULL, NULL, NULL, NULL, '教工组', 'UNIVERSITY', 1, 4),
(5, NULL, NULL, '高一', '1班', NULL, 'K12', 1, 5),
(6, NULL, NULL, '高一', '2班', NULL, 'K12', 1, 6),
(7, '计算机科学与技术学院', '本科', NULL, '大一', NULL, 'UNIVERSITY', 1, 7),
(8, '计算机科学与技术学院', '本科', NULL, '大二', NULL, 'UNIVERSITY', 1, 8),
(9, NULL, NULL, '九年级', '3班', NULL, 'HIGH_SCHOOL', 1, 9);

-- ----------------------------
-- 3. Initial Events (Demo Data)
-- ----------------------------
-- Event 1: Spring Sports Meeting (Draft)
INSERT INTO `event` (`id`, `name`, `description`, `reg_start_time`, `reg_deadline`, `start_time`, `end_time`, `max_items_per_athlete`, `status`, `school_id`, `create_time`) VALUES
(1, '2026年春季田径运动会', '一年一度的春季运动会，欢迎全校师生踊跃报名！', '2026-03-10 08:00:00', '2026-03-20 18:00:00', '2026-04-01 09:00:00', '2026-04-03 17:00:00', 3, 'DRAFT', 1, NOW());

-- Event 2: Autumn Sports Meeting (Published)
INSERT INTO `event` (`id`, `name`, `description`, `reg_start_time`, `reg_deadline`, `start_time`, `end_time`, `max_items_per_athlete`, `status`, `school_id`, `create_time`) VALUES
(2, '2025年秋季趣味运动会', '趣味项目为主，重在参与，增进友谊。', '2025-09-01 08:00:00', '2025-09-15 18:00:00', '2025-09-25 09:00:00', '2025-09-26 17:00:00', 3, 'OPEN', 1, NOW());

-- ----------------------------
-- 4. Initial Event Items (Projects) for Event 1
-- ----------------------------
INSERT INTO `event_item` (`id`, `event_id`, `name`, `gender_limit`, `limit_dept_ids`, `max_count`, `current_count`, `school_id`, `create_time`) VALUES
(1, 1, '男子100米', 'MALE', NULL, 30, 0, 1, NOW()),
(2, 1, '女子100米', 'FEMALE', NULL, 30, 0, 1, NOW()),
(3, 1, '男子400米', 'MALE', NULL, 20, 0, 1, NOW()),
(4, 1, '女子400米', 'FEMALE', NULL, 20, 0, 1, NOW()),
(5, 1, '男子跳远', 'MALE', NULL, 15, 0, 1, NOW()),
(6, 1, '女子跳远', 'FEMALE', NULL, 15, 0, 1, NOW()),
(7, 1, '男子铅球', 'MALE', NULL, 10, 0, 1, NOW()),
(8, 1, '女子铅球', 'FEMALE', NULL, 10, 0, 1, NOW()),
(9, 1, '教工男子100米', 'MALE', '[4]', 20, 0, 1, NOW()),
(10, 1, '教工女子100米', 'FEMALE', '[4]', 20, 0, 1, NOW());

-- ----------------------------
-- 5. Initial Event Items (Projects) for Event 2
-- ----------------------------
INSERT INTO `event_item` (`id`, `event_id`, `name`, `gender_limit`, `limit_dept_ids`, `max_count`, `current_count`, `school_id`, `create_time`) VALUES
(11, 2, '二人三足', 'ALL', NULL, 50, 0, 1, NOW()),
(12, 2, '袋鼠跳', 'ALL', NULL, 50, 0, 1, NOW());

-- ----------------------------
-- 6. Eligibility Rules for Event Items
--     Auto-generated from gender_limit + limit_dept_ids
-- ----------------------------
-- Config: 教工男子100米 (gender_limit=MALE, limit_dept_ids='[4]')
INSERT INTO eligibility_config (owner_type, owner_id, group_combination, enabled, create_time) VALUES ('EVENT_ITEM', 9, 'AND', 1, NOW());
SET @cfg9 = LAST_INSERT_ID();
INSERT INTO eligibility_group (config_id, group_logic, group_desc, sort_order, create_time) VALUES (@cfg9, 'AND', NULL, 0, NOW());
SET @grp9 = LAST_INSERT_ID();
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time) VALUES (@grp9, 'GENDER', 'EQ', '"男"', 0, NOW());
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time) VALUES (@grp9, 'DEPT', 'IN', '[4]', 1, NOW());

-- Config: 教工女子100米 (gender_limit=FEMALE, limit_dept_ids='[4]')
INSERT INTO eligibility_config (owner_type, owner_id, group_combination, enabled, create_time) VALUES ('EVENT_ITEM', 10, 'AND', 1, NOW());
SET @cfg10 = LAST_INSERT_ID();
INSERT INTO eligibility_group (config_id, group_logic, group_desc, sort_order, create_time) VALUES (@cfg10, 'AND', NULL, 0, NOW());
SET @grp10 = LAST_INSERT_ID();
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time) VALUES (@grp10, 'GENDER', 'EQ', '"女"', 0, NOW());
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time) VALUES (@grp10, 'DEPT', 'IN', '[4]', 1, NOW());

-- Config: 男子项目 (gender_limit=MALE) — 项目1,3,5,7
INSERT INTO eligibility_config (owner_type, owner_id, group_combination, enabled, create_time)
SELECT 'EVENT_ITEM', id, 'AND', 1, NOW() FROM event_item WHERE id IN (1,3,5,7);
INSERT INTO eligibility_group (config_id, group_logic, group_desc, sort_order, create_time)
SELECT ec.id, 'AND', NULL, 0, NOW() FROM eligibility_config ec WHERE ec.owner_id IN (1,3,5,7);
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time)
SELECT eg.id, 'GENDER', 'EQ', '"男"', 0, NOW()
FROM eligibility_group eg JOIN eligibility_config ec ON eg.config_id = ec.id
WHERE ec.owner_id IN (1,3,5,7);

-- Config: 女子项目 (gender_limit=FEMALE) — 项目2,4,6,8
INSERT INTO eligibility_config (owner_type, owner_id, group_combination, enabled, create_time)
SELECT 'EVENT_ITEM', id, 'AND', 1, NOW() FROM event_item WHERE id IN (2,4,6,8);
INSERT INTO eligibility_group (config_id, group_logic, group_desc, sort_order, create_time)
SELECT ec.id, 'AND', NULL, 0, NOW() FROM eligibility_config ec WHERE ec.owner_id IN (2,4,6,8);
INSERT INTO eligibility_rule (group_id, dimension, operator, value_json, sort_order, create_time)
SELECT eg.id, 'GENDER', 'EQ', '"女"', 0, NOW()
FROM eligibility_group eg JOIN eligibility_config ec ON eg.config_id = ec.id
WHERE ec.owner_id IN (2,4,6,8);

SET FOREIGN_KEY_CHECKS=1;
