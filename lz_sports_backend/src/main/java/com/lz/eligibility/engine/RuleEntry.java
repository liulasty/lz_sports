package com.lz.eligibility.engine;

import lombok.Value;

/**
 * 单条规则 —— 引擎内部模型（区别于实体 EligibilityRule）。
 */
@Value
public class RuleEntry {
    Dimension dimension;
    Operator operator;
    /** raw value as parsed, type depends on dimension+operator */
    Object value;
}
