/*
 * Update: V1.2
 * Date: 2024-03-03
 * Purpose: Add event_admin_mapping table (Phase 3.2)
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. Create event_admin_mapping table
-- ----------------------------
DROP TABLE IF EXISTS `event_admin_mapping`;
CREATE TABLE `event_admin_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint(20) NOT NULL COMMENT '赛事ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID(赛事管理员)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_user` (`event_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事管理员关联表';

SET FOREIGN_KEY_CHECKS = 1;
