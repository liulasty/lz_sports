package com.lz.eligibility.engine;

/**
 * 规则运算符。
 * 各运算符适用的维度见 RuleEvaluator 校验逻辑。
 */
public enum Operator {
    EQ,       // 等于（单值, 如 GENDER EQ "男"）
    IN,       // 在集合中（多值, 如 GRADE IN ["大一","大二"]）
    NOT_IN,   // 不在集合中
    BETWEEN,  // 区间（{min, max}, 仅 AGE）
    GTE,      // 大于等于（单值, 仅 AGE）
    LTE       // 小于等于（单值, 仅 AGE）
}
