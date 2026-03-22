-- 添加 event_id 字段
ALTER TABLE `athlete` ADD COLUMN `event_id` bigint NOT NULL COMMENT '赛事ID' AFTER `user_id`;

-- 修改 uk_user_id 唯一索引为 uk_user_event
ALTER TABLE `athlete` DROP INDEX `uk_user_id`;
ALTER TABLE `athlete` ADD UNIQUE KEY `uk_user_event` (`user_id`, `event_id`);

-- 修改状态默认值和注释
ALTER TABLE `athlete` MODIFY COLUMN `athlete_state` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED';
