package com.lz.eligibility.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleEvaluator 纯逻辑单测 —— 覆盖 T1~T7。
 * 无需 Spring 上下文，毫秒级运行。
 */
class RuleEvaluatorTest {

    private final RuleEvaluator evaluator = new RuleEvaluator();

    // ========== T1: 无规则 = 全体通过 ==========

    @Test
    void t1_noRules_allPass() {
        AthleteContext ctx = anyAthlete();
        assertTrue(evaluator.evaluate(ctx, List.of(), GroupLogic.AND).isPassed());
        assertTrue(evaluator.evaluate(ctx, null, GroupLogic.AND).isPassed());
    }

    // ========== T2: 男子大一 = 1 组 AND ==========

    @Test
    void t2_maleFreshman_andGroup() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "男子大一", List.of(
                new RuleEntry(Dimension.GENDER, Operator.EQ, "男"),
                new RuleEntry(Dimension.GRADE, Operator.IN, List.of("大一"))
        ));

        AthleteContext maleF1 = AthleteContext.builder().gender("男").grade("大一").build();
        AthleteContext femaleF1 = AthleteContext.builder().gender("女").grade("大一").build();
        AthleteContext maleF2 = AthleteContext.builder().gender("男").grade("大二").build();

        assertAll(
                () -> assertTrue(evaluator.evaluate(maleF1, List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(femaleF1, List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(maleF2, List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== T3: 大一男 OR 大二女 = 2 组 OR ==========

    @Test
    void t3_freshmanMale_or_sophomoreFemale() {
        GroupEntry g1 = new GroupEntry(GroupLogic.AND, "大一男", List.of(
                new RuleEntry(Dimension.GENDER, Operator.EQ, "男"),
                new RuleEntry(Dimension.GRADE, Operator.EQ, "大一")
        ));
        GroupEntry g2 = new GroupEntry(GroupLogic.AND, "大二女", List.of(
                new RuleEntry(Dimension.GENDER, Operator.EQ, "女"),
                new RuleEntry(Dimension.GRADE, Operator.EQ, "大二")
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().gender("男").grade("大一").build(),
                        List.of(g1, g2), GroupLogic.OR).isPassed()),
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().gender("女").grade("大二").build(),
                        List.of(g1, g2), GroupLogic.OR).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("男").grade("大二").build(),
                        List.of(g1, g2), GroupLogic.OR).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("女").grade("大一").build(),
                        List.of(g1, g2), GroupLogic.OR).isPassed())
        );
    }

    // ========== T4: 多学院本科女生 = AND ==========

    @Test
    void t4_multiCollegeFemale_and() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "多学院女生", List.of(
                new RuleEntry(Dimension.GENDER, Operator.EQ, "女"),
                new RuleEntry(Dimension.COLLEGE, Operator.IN, List.of("计算机学院", "软件学院")),
                new RuleEntry(Dimension.GRADE, Operator.IN, List.of("大一", "大二", "大三", "大四"))
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().gender("女").college("计算机学院").grade("大二").build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("女").college("外国语学院").grade("大二").build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("男").college("计算机学院").grade("大二").build(),
                        List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== T5: K12 小学部 = GRADE IN [1-6] ==========

    @Test
    void t5_k12_primary_gradeIn() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "小学部", List.of(
                new RuleEntry(Dimension.GRADE, Operator.IN, List.of("一年级", "二年级", "三年级", "四年级", "五年级", "六年级"))
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().grade("一年级").build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().grade("初一").build(),
                        List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== T6: 迁移一致性 ==========

    @Test
    void t6_migrationConsistency() {
        // 旧: gender_limit=MALE, limit_dept_ids=[1,2,3]
        // → 新: GENDER EQ "男" + DEPT IN [1,2,3]
        GroupEntry group = new GroupEntry(GroupLogic.AND, "迁移兼容", List.of(
                new RuleEntry(Dimension.GENDER, Operator.EQ, "男"),
                new RuleEntry(Dimension.DEPT, Operator.IN, List.of(1L, 2L, 3L))
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().gender("男").deptId(2L).ancestorDeptIds(List.of()).build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("女").deptId(2L).ancestorDeptIds(List.of()).build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().gender("男").deptId(99L).ancestorDeptIds(List.of()).build(),
                        List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== T7: 清空规则 = 全体可报 ==========

    @Test
    void t7_clearedRules_allPass() {
        AthleteContext ctx = anyAthlete();
        assertTrue(evaluator.evaluate(ctx, List.of(), GroupLogic.AND).isPassed());
        assertTrue(evaluator.evaluate(ctx, null, GroupLogic.AND).isPassed());
    }

    // ========== 额外：AGE BETWEEN ==========

    @Test
    void ageBetween_shouldWork() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "少年组", List.of(
                new RuleEntry(Dimension.AGE, Operator.BETWEEN, Map.of("min", 16, "max", 18))
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().age(17).build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().age(15).build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().age(19).build(),
                        List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== 额外：DEPT 祖先节点匹配 ==========

    @Test
    void dept_ancestorMatching() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "学院限制", List.of(
                new RuleEntry(Dimension.DEPT, Operator.IN, List.of(100L))
        ));

        assertTrue(evaluator.evaluate(
                AthleteContext.builder().deptId(100L).ancestorDeptIds(List.of()).build(),
                List.of(group), GroupLogic.AND).isPassed());
        assertTrue(evaluator.evaluate(
                AthleteContext.builder().deptId(200L).ancestorDeptIds(List.of(100L)).build(),
                List.of(group), GroupLogic.AND).isPassed());
        assertFalse(evaluator.evaluate(
                AthleteContext.builder().deptId(300L).ancestorDeptIds(List.of()).build(),
                List.of(group), GroupLogic.AND).isPassed());
    }

    // ========== 额外：NOT_IN ==========

    @Test
    void notIn_shouldWork() {
        GroupEntry group = new GroupEntry(GroupLogic.AND, "非管理专业", List.of(
                new RuleEntry(Dimension.MAJOR, Operator.NOT_IN, List.of("工商管理", "行政管理"))
        ));

        assertAll(
                () -> assertTrue(evaluator.evaluate(
                        AthleteContext.builder().major("计算机科学").build(),
                        List.of(group), GroupLogic.AND).isPassed()),
                () -> assertFalse(evaluator.evaluate(
                        AthleteContext.builder().major("工商管理").build(),
                        List.of(group), GroupLogic.AND).isPassed())
        );
    }

    // ========== helpers ==========

    private static AthleteContext anyAthlete() {
        return AthleteContext.builder().gender("男").build();
    }
}
