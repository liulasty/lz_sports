/*
 * Migration: V1.2__add_verify_token
 * Description: Add verify_token column to sys_user table for registration process refactoring.
 */

ALTER TABLE sys_user ADD COLUMN verify_token VARCHAR(64) DEFAULT NULL COMMENT '验证令牌';
CREATE UNIQUE INDEX idx_sys_user_verify_token ON sys_user(verify_token);
