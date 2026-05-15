package com.lz.eligibility.engine;

import lombok.Value;

import java.util.List;

/**
 * 规则组 —— 引擎内部模型（区别于实体 EligibilityGroup）。
 */
@Value
public class GroupEntry {
    GroupLogic groupLogic;
    String groupDesc;
    List<RuleEntry> rules;
}
