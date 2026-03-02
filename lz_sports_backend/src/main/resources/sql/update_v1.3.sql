/*
 * Update: V1.3
 * Date: 2024-03-03
 * Purpose: Add score table (Phase 4.2)
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. Create score table
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `registration_id` bigint(20) NOT NULL COMMENT '报名ID',
  `event_id` bigint(20) NOT NULL COMMENT '赛事ID',
  `item_id` bigint(20) NOT NULL COMMENT '项目ID',
  `athlete_id` bigint(20) NOT NULL COMMENT '运动员ID',
  `score_value` varchar(50) DEFAULT NULL COMMENT '成绩数值(如 10.5s, 1.8m)',
  `score_rank` int(11) DEFAULT NULL COMMENT '名次',
  `is_published` tinyint(1) DEFAULT 0 COMMENT '是否发布: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registration` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

SET FOREIGN_KEY_CHECKS = 1;
