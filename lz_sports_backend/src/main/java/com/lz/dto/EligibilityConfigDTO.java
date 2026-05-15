package com.lz.dto;

import lombok.Data;

import java.util.List;

/**
 * 规则配置 DTO —— 与前端对齐的全量覆盖结构。
 */
@Data
public class EligibilityConfigDTO {
    /** 组间 AND/OR */
    private String groupCombination;
    /** 是否启用 */
    private Boolean enabled;
    /** 规则组列表 */
    private List<GroupDTO> groups;

    @Data
    public static class GroupDTO {
        /** 组内 AND/OR */
        private String groupLogic;
        /** 可读描述，如"大一男生" */
        private String groupDesc;
        /** 规则列表 */
        private List<RuleDTO> rules;
    }

    @Data
    public static class RuleDTO {
        private String dimension;
        private String operator;
        /** 规则值（Jackson 可反序列化的任意类型） */
        private Object value;
    }
}
