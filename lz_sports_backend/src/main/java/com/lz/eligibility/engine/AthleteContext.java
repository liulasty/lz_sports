package com.lz.eligibility.engine;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 运动员上下文 —— 引擎校验时需要的所有维度值。
 * 由 AthleteContextResolver 从 DB 组装，RuleEvaluator 只读使用。
 * <p>
 * 单值字段存「运动员自身的值」，
 * ancestorDeptIds 存该部门在部门树中所有祖先节点的 ID，
 * 用于 DEPT IN/NOT_IN 的层级匹配。
 */
@Value
@Builder
public class AthleteContext {
    String gender;
    Long deptId;
    List<Long> ancestorDeptIds;

    String college;
    String major;
    String grade;
    String className;

    /** 可能为 null（年龄字段在 athlete 表中是字符串，可能无法解析） */
    Integer age;
}
