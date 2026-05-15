package com.lz.eligibility.engine;

/**
 * 规则维度枚举。
 * 每个维度对应 AthleteContext 中的一个字段，由 AthleteContextResolver 负责填充。
 */
public enum Dimension {
    GENDER,   // value: "男"/"女"
    DEPT,     // value: Long (deptId)
    COLLEGE,  // value: String
    MAJOR,    // value: String
    GRADE,    // value: String
    CLASS,    // value: String
    AGE       // value: Integer (由 athlete.age 解析)
}
