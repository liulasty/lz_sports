USE `lz_sports`;

ALTER TABLE `department` ADD COLUMN `org_mode` varchar(20) DEFAULT 'UNIVERSITY' COMMENT '组织架构模式: UNIVERSITY/K12' AFTER `dept_name`;

-- Update existing records to match K12 if grade is not null
UPDATE `department` SET `org_mode` = 'K12' WHERE `grade` IS NOT NULL;
