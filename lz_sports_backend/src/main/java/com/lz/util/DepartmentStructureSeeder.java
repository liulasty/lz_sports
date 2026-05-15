package com.lz.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.entity.Department;
import com.lz.mapper.DepartmentMapper;

import java.util.List;
import java.util.Locale;

/**
 * 为仅有学院/年级顶层的组织数据，补全默认专业与班级（如大一、大二 / 1班、2班）。
 */
public final class DepartmentStructureSeeder {

    public static final String DEFAULT_UNIVERSITY_MAJOR = "本科";
    public static final String[] DEFAULT_UNIVERSITY_CLASSES = {"大一", "大二", "大三", "大四"};
    public static final String[] DEFAULT_K12_CLASSES = {"1班", "2班", "3班"};

    private DepartmentStructureSeeder() {
    }

    public static boolean isK12Mode(String orgMode) {
        if (orgMode == null) {
            return false;
        }
        String normalized = orgMode.trim().toUpperCase(Locale.ROOT);
        return "K12".equals(normalized) || "HIGH_SCHOOL".equals(normalized);
    }

    /**
     * 幂等：若某学院/年级下尚无班级行，则按默认模板插入。
     *
     * @return 新增部门行数
     */
    public static int ensureDefaultClassStructure(DepartmentMapper departmentMapper, Long schoolId, String orgMode) {
        if (schoolId == null || orgMode == null || orgMode.isBlank()) {
            return 0;
        }
        String mode = orgMode.trim().toUpperCase(Locale.ROOT);
        List<Department> all = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getSchoolId, schoolId)
                .eq(Department::getOrgMode, mode)
                .orderByAsc(Department::getSortOrder));
        if (all.isEmpty()) {
            return 0;
        }
        int inserted = 0;
        if (isK12Mode(mode)) {
            inserted += seedK12Classes(departmentMapper, schoolId, mode, all);
        } else {
            inserted += seedUniversityClasses(departmentMapper, schoolId, mode, all);
        }
        return inserted;
    }

    private static int seedUniversityClasses(
            DepartmentMapper departmentMapper, Long schoolId, String orgMode, List<Department> all) {
        int inserted = 0;
        int sortBase = nextSortOrder(all);
        for (Department top : all) {
            if (top.getCollege() == null || top.getCollege().isBlank()) {
                continue;
            }
            if (hasUniversityClassUnder(all, top.getCollege())) {
                continue;
            }
            String college = top.getCollege().trim();
            int order = 0;
            for (String className : DEFAULT_UNIVERSITY_CLASSES) {
                Department row = new Department();
                row.setCollege(college);
                row.setMajor(DEFAULT_UNIVERSITY_MAJOR);
                row.setClassName(className);
                row.setOrgMode(orgMode);
                row.setSchoolId(schoolId);
                row.setSortOrder(sortBase + order++);
                departmentMapper.insert(row);
                inserted++;
            }
        }
        return inserted;
    }

    private static int seedK12Classes(
            DepartmentMapper departmentMapper, Long schoolId, String orgMode, List<Department> all) {
        int inserted = 0;
        int sortBase = nextSortOrder(all);
        for (Department top : all) {
            if (top.getGrade() == null || top.getGrade().isBlank()) {
                continue;
            }
            if (hasK12ClassUnder(all, top.getGrade())) {
                continue;
            }
            String grade = top.getGrade().trim();
            int order = 0;
            for (String className : DEFAULT_K12_CLASSES) {
                Department row = new Department();
                row.setGrade(grade);
                row.setClassName(className);
                row.setOrgMode(orgMode);
                row.setSchoolId(schoolId);
                row.setSortOrder(sortBase + order++);
                departmentMapper.insert(row);
                inserted++;
            }
        }
        return inserted;
    }

    private static boolean hasUniversityClassUnder(List<Department> all, String college) {
        return all.stream().anyMatch(d ->
                college.equals(d.getCollege())
                        && d.getClassName() != null
                        && !d.getClassName().isBlank());
    }

    private static boolean hasK12ClassUnder(List<Department> all, String grade) {
        return all.stream().anyMatch(d ->
                grade.equals(d.getGrade())
                        && d.getClassName() != null
                        && !d.getClassName().isBlank());
    }

    private static int nextSortOrder(List<Department> all) {
        return all.stream()
                .map(Department::getSortOrder)
                .filter(o -> o != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
