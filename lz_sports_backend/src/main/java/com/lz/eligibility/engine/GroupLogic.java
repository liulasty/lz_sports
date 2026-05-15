package com.lz.eligibility.engine;

/**
 * 规则组内 / 组间逻辑。
 * AND: 全部满足才通过
 * OR:  任一满足即通过
 */
public enum GroupLogic {
    AND,
    OR
}
