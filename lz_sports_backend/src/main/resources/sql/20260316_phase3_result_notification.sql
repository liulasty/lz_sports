/*
 * Migration: 20260316_phase3_result_notification
 * Date: 2026-03-16
 * Purpose: phase3_result_notification
 * 
 * Operations:
 * [x] Add
 * [ ] Delete
 * [x] Adjust
 *
 * Description:
 * 为成绩发布与站内信补齐字段与索引：
 * 1) result 新增 published_at、remark 字段；
 * 2) sys_user 新增 unread_count 字段并回填；
 * 3) result.registration_id 唯一索引兜底创建；
 * 4) notification 增加用户+时间索引提升列表查询。
 */

ALTER TABLE `result`
    ADD COLUMN IF NOT EXISTS `published_at` datetime DEFAULT NULL COMMENT '成绩发布时间' AFTER `is_published`,
    ADD COLUMN IF NOT EXISTS `remark` varchar(255) DEFAULT NULL COMMENT '成绩备注' AFTER `score_rank`;

ALTER TABLE `sys_user`
    ADD COLUMN IF NOT EXISTS `unread_count` int NOT NULL DEFAULT 0 COMMENT '未读站内信数量' AFTER `status`;

UPDATE `sys_user`
SET `unread_count` = 0
WHERE `unread_count` IS NULL;

ALTER TABLE `result`
    ADD UNIQUE INDEX IF NOT EXISTS `uk_registration` (`registration_id`);

ALTER TABLE `notification`
    ADD INDEX IF NOT EXISTS `idx_user_create_time` (`user_id`, `create_time`);
