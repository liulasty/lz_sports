/*
 * Migration: 20260315_phase1_user_first_login
 * Date: 2026-03-15 12:00:00
 * Purpose: add_user_first_login_flag
 *
 * Operations:
 * [x] Add
 * [ ] Delete
 * [x] Adjust
 *
 * Description:
 * 为 sys_user 增加 is_first_login 字段，用于控制首次登录强制改密。
 * 同时回填历史数据，避免空值。
 */

ALTER TABLE `sys_user`
    ADD COLUMN `is_first_login` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否首次登录：0否1是' AFTER `user_type`;

UPDATE `sys_user`
SET `is_first_login` = 0
WHERE `is_first_login` IS NULL;
