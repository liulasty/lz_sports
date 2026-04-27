/*
 * Migration: 20260427214316_add_athlete_reject_reason
 * Date: 2026-04-27 21:43:16
 * Purpose: add_athlete_reject_reason
 *
 * Operations:
 * [x] Add
 * [ ] Delete
 * [x] Adjust
 *
 * Description:
 * Add reject_reason column for athlete applications to persist rejection reasons.
 * Backfill historical records to default '无' when value is NULL/blank, and set
 * column default to '无' for defensive compatibility.
 */

-- Write your SQL commands below this line
-- ---------------------------------------

ALTER TABLE athlete
    ADD COLUMN reject_reason VARCHAR(255) NOT NULL DEFAULT '无' COMMENT '驳回原因';

UPDATE athlete
SET reject_reason = '无'
WHERE reject_reason IS NULL OR TRIM(reject_reason) = '';

