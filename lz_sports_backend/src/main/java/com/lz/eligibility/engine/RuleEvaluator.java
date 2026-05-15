package com.lz.eligibility.engine;

import java.util.List;
import java.util.Map;

/**
 * 资格规则校验引擎 —— 纯内存计算，无任何外部依赖。
 * <p>
 * 输入：AthleteContext + 规则组列表 + 组间组合逻辑
 * 输出：EligibilityResult（通过/失败 + 失败原因）
 */
public class RuleEvaluator {

    public EligibilityResult evaluate(AthleteContext context,
                                      List<GroupEntry> groups,
                                      GroupLogic groupCombination) {
        if (groups == null || groups.isEmpty()) {
            return EligibilityResult.ok();
        }

        if (groupCombination == GroupLogic.AND) {
            for (GroupEntry group : groups) {
                EligibilityResult r = evaluateGroup(context, group);
                if (!r.isPassed()) {
                    return r;
                }
            }
            return EligibilityResult.ok();
        } else {
            String firstFailReason = null;
            for (GroupEntry group : groups) {
                EligibilityResult r = evaluateGroup(context, group);
                if (r.isPassed()) {
                    return EligibilityResult.ok();
                }
                if (firstFailReason == null) {
                    firstFailReason = r.getReason();
                }
            }
            return EligibilityResult.fail(firstFailReason != null ? firstFailReason : "未满足任何资格条件");
        }
    }

    private EligibilityResult evaluateGroup(AthleteContext context, GroupEntry group) {
        List<RuleEntry> rules = group.getRules();
        if (rules == null || rules.isEmpty()) {
            return EligibilityResult.ok();
        }

        if (group.getGroupLogic() == GroupLogic.AND) {
            for (RuleEntry rule : rules) {
                if (!evaluateRule(context, rule)) {
                    return EligibilityResult.fail(buildFailureReason(group.getGroupDesc(), rule));
                }
            }
            return EligibilityResult.ok();
        } else {
            for (RuleEntry rule : rules) {
                if (evaluateRule(context, rule)) {
                    return EligibilityResult.ok();
                }
            }
            String reason = group.getGroupDesc() != null
                    ? "不满足[" + group.getGroupDesc() + "]中的任一条件"
                    : "不满足组内任一条件";
            return EligibilityResult.fail(reason);
        }
    }

    // ---- single rule evaluation ----

    boolean evaluateRule(AthleteContext context, RuleEntry rule) {
        Object actual = resolveDimensionValue(context, rule.getDimension());

        if (rule.getDimension() == Dimension.DEPT) {
            return evaluateDeptRule(context, rule);
        }

        if (actual == null) {
            return false;
        }

        switch (rule.getOperator()) {
            case EQ:
                return eq(actual, rule.getValue());
            case IN:
                return in(actual, rule.getValue());
            case NOT_IN:
                return !in(actual, rule.getValue());
            case BETWEEN:
                return between(actual, rule.getValue());
            case GTE:
                return gte(actual, rule.getValue());
            case LTE:
                return lte(actual, rule.getValue());
            default:
                return false;
        }
    }

    private boolean evaluateDeptRule(AthleteContext context, RuleEntry rule) {
        List<Long> allDeptIds = new java.util.ArrayList<>();
        if (context.getDeptId() != null) {
            allDeptIds.add(context.getDeptId());
        }
        if (context.getAncestorDeptIds() != null) {
            for (Long id : context.getAncestorDeptIds()) {
                if (id != null && !allDeptIds.contains(id)) {
                    allDeptIds.add(id);
                }
            }
        }
        if (allDeptIds.isEmpty()) return false;

        Object ruleValue = rule.getValue();
        switch (rule.getOperator()) {
            case IN:
                for (Long deptId : allDeptIds) {
                    if (in(deptId, ruleValue)) return true;
                }
                return false;
            case NOT_IN:
                for (Long deptId : allDeptIds) {
                    if (in(deptId, ruleValue)) return false;
                }
                return true;
            case EQ:
                return in(context.getDeptId(), ruleValue);
            default:
                return false;
        }
    }

    // ---- dimension value extraction ----

    private Object resolveDimensionValue(AthleteContext ctx, Dimension dim) {
        switch (dim) {
            case GENDER:  return ctx.getGender();
            case DEPT:    return ctx.getDeptId();
            case COLLEGE: return ctx.getCollege();
            case MAJOR:   return ctx.getMajor();
            case GRADE:   return ctx.getGrade();
            case CLASS:   return ctx.getClassName();
            case AGE:     return ctx.getAge();
            default:      return null;
        }
    }

    // ---- operator implementations ----

    private boolean eq(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        if (actual instanceof String && expected instanceof String) {
            return ((String) actual).equalsIgnoreCase((String) expected);
        }
        return actual.equals(expected);
    }

    @SuppressWarnings("unchecked")
    private boolean in(Object actual, Object collectionValue) {
        if (actual == null || collectionValue == null) return false;
        if (collectionValue instanceof List) {
            for (Object item : (List<Object>) collectionValue) {
                if (eq(actual, item)) return true;
            }
            return false;
        }
        if (collectionValue instanceof java.util.Collection) {
            for (Object item : (java.util.Collection<Object>) collectionValue) {
                if (eq(actual, item)) return true;
            }
            return false;
        }
        return eq(actual, collectionValue);
    }

    private boolean between(Object actual, Object rangeValue) {
        if (!(actual instanceof Number) || !(rangeValue instanceof Map)) return false;
        Number num = (Number) actual;
        Map<String, Object> range = (Map<String, Object>) rangeValue;
        Number min = (Number) range.get("min");
        Number max = (Number) range.get("max");
        if (min != null && num.doubleValue() < min.doubleValue()) return false;
        if (max != null && num.doubleValue() > max.doubleValue()) return false;
        return true;
    }

    private boolean gte(Object actual, Object expected) {
        if (!(actual instanceof Number) || !(expected instanceof Number)) return false;
        return ((Number) actual).doubleValue() >= ((Number) expected).doubleValue();
    }

    private boolean lte(Object actual, Object expected) {
        if (!(actual instanceof Number) || !(expected instanceof Number)) return false;
        return ((Number) actual).doubleValue() <= ((Number) expected).doubleValue();
    }

    // ---- failure reason ----

    private String buildFailureReason(String groupDesc, RuleEntry rule) {
        StringBuilder sb = new StringBuilder();
        if (groupDesc != null && !groupDesc.isEmpty()) {
            sb.append("[").append(groupDesc).append("]");
        }
        sb.append("不满足条件: ")
                .append(rule.getDimension().name())
                .append(" ").append(rule.getOperator().name())
                .append(" ").append(rule.getValue());
        return sb.toString();
    }
}
