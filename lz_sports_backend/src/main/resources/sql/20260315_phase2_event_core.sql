/*
 * Migration: 20260315_phase2_event_core
 * Date: 2026-03-15
 * Purpose: phase2_event_core
 * 
 * Operations:
 * [x] Add
 * [ ] Delete
 * [x] Adjust
 *
 * Description:
 * Add phase2 required fields for event and event_item.
 */

ALTER TABLE `event`
    ADD COLUMN IF NOT EXISTS `max_items_per_athlete` int DEFAULT 3 COMMENT '每位运动员可报名项目上限';

ALTER TABLE `event_item`
    ADD COLUMN IF NOT EXISTS `category` varchar(20) DEFAULT 'CUSTOM' COMMENT '项目类别: STANDARD/CUSTOM',
    ADD COLUMN IF NOT EXISTS `start_time` datetime DEFAULT NULL COMMENT '项目开始时间',
    ADD COLUMN IF NOT EXISTS `end_time` datetime DEFAULT NULL COMMENT '项目结束时间';
