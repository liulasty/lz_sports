package com.lz.eligibility.resolver;

import com.lz.eligibility.engine.AthleteContext;
import com.lz.entity.Athlete;
import com.lz.entity.Department;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.DepartmentMapper;
import com.lz.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 从 DB 加载运动员维度数据，组装 AthleteContext。
 */
@Component
@RequiredArgsConstructor
public class AthleteContextResolver {

    private final AthleteMapper athleteMapper;
    private final DepartmentMapper departmentMapper;    // direct mapper to avoid service-level cascading
    private final DepartmentService departmentService;  // for getFullDepartmentName / list

    /**
     * @param userId  系统用户 ID
     * @param eventId 赛事 ID（athlete 是赛事维度的）
     * @return AthleteContext，若运动员不存在则返回 null
     */
    public AthleteContext resolve(Long userId, Long eventId) {
        Athlete athlete = athleteMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Athlete>()
                        .eq(Athlete::getUserId, userId)
                        .eq(Athlete::getEventId, eventId));
        if (athlete == null) return null;

        Department dept = athlete.getDeptId() != null
                ? departmentMapper.selectById(athlete.getDeptId())
                : null;

        Integer age = parseAge(athlete.getAge());
        List<Long> ancestorDeptIds = dept != null ? resolveAncestorDeptIds(dept) : List.of();

        return AthleteContext.builder()
                .gender(athlete.getGender())
                .deptId(athlete.getDeptId())
                .ancestorDeptIds(ancestorDeptIds)
                .college(dept != null ? dept.getCollege() : null)
                .major(dept != null ? dept.getMajor() : null)
                .grade(dept != null ? dept.getGrade() : null)
                .className(dept != null ? dept.getClassName() : null)
                .age(age)
                .build();
    }

    /**
     * 解析 department 宽表中的祖先节点 ID。
     * 例如：用户部门是"软工1班"，ancestors 包含"软件工程"专业和"计算机学院"的部门 ID。
     */
    List<Long> resolveAncestorDeptIds(Department dept) {
        List<Long> ancestors = new ArrayList<>();
        if (dept == null) return ancestors;

        boolean isK12 = "K12".equalsIgnoreCase(dept.getOrgMode()) || "HIGH_SCHOOL".equalsIgnoreCase(dept.getOrgMode());

        if (isK12) {
            // K12: 找同年级且无 className 的 grade 级别部门
            if (dept.getGrade() != null) {
                List<Department> grades = departmentMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Department>()
                                .eq(Department::getGrade, dept.getGrade())
                                .eq(Department::getOrgMode, dept.getOrgMode())
                                .isNull(Department::getClassName)
                                .or(w -> w.eq(Department::getClassName, "")));
                for (Department d : grades) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) {
                        ancestors.add(d.getId());
                    }
                }
            }
        } else {
            // UNIVERSITY: 先找同专业（同college+同major）的 major 级部门，再找同college 的 college 级部门
            if (dept.getMajor() != null && !dept.getMajor().isEmpty()) {
                List<Department> majors = departmentMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Department>()
                                .eq(Department::getCollege, dept.getCollege())
                                .eq(Department::getMajor, dept.getMajor())
                                .and(w -> w.isNull(Department::getClassName)
                                        .or(w2 -> w2.eq(Department::getClassName, ""))));
                for (Department d : majors) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) {
                        ancestors.add(d.getId());
                    }
                }
            }
            if (dept.getCollege() != null) {
                List<Department> colleges = departmentMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Department>()
                                .eq(Department::getCollege, dept.getCollege())
                                .and(w -> w.isNull(Department::getMajor)
                                        .or(w2 -> w2.eq(Department::getMajor, "")))
                                .and(w -> w.isNull(Department::getClassName)
                                        .or(w2 -> w2.eq(Department::getClassName, ""))));
                for (Department d : colleges) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) {
                        ancestors.add(d.getId());
                    }
                }
            }
        }

        return ancestors;
    }

    private Integer parseAge(String age) {
        if (age == null || age.isBlank()) return null;
        try {
            return Integer.parseInt(age.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
