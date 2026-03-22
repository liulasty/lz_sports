/*
 * Migration: V1.3__update_sys_user_defaults
 * Date: 2026-03-22
 * Purpose: Set default values for gender and name in sys_user table
 *
 * Description:
 * Alter gender and name columns to have default values. Update existing nulls.
 */

-- Write your SQL commands below this line
-- ---------------------------------------

ALTER TABLE sys_user ALTER gender SET DEFAULT 'UNKNOWN';
ALTER TABLE sys_user ALTER name SET DEFAULT '';

UPDATE sys_user SET gender='UNKNOWN', name='' WHERE gender IS NULL OR name IS NULL;
