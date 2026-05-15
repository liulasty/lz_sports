package com.lz.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.entity.Department;
import com.lz.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentStructureSeederTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @Test
    void seedsUniversityClassesWhenOnlyCollegeExists() {
        Department college = new Department();
        college.setId(1L);
        college.setCollege("计算机学院");
        college.setOrgMode("UNIVERSITY");
        college.setSchoolId(1L);
        college.setSortOrder(1);
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(college));

        int inserted = DepartmentStructureSeeder.ensureDefaultClassStructure(departmentMapper, 1L, "UNIVERSITY");

        assertEquals(4, inserted);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper, atLeastOnce()).insert(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(d -> "大一".equals(d.getClassName())));
    }

    @Test
    void seedsK12ClassesWhenOnlyGradeExists() {
        Department grade = new Department();
        grade.setId(2L);
        grade.setGrade("九年级");
        grade.setOrgMode("HIGH_SCHOOL");
        grade.setSchoolId(1L);
        grade.setSortOrder(1);
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(grade));

        int inserted = DepartmentStructureSeeder.ensureDefaultClassStructure(departmentMapper, 1L, "HIGH_SCHOOL");

        assertEquals(3, inserted);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper, atLeastOnce()).insert(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(d -> "3班".equals(d.getClassName())));
    }

    @Test
    void isIdempotentWhenClassesAlreadyExist() {
        List<Department> existing = new ArrayList<>();
        Department college = new Department();
        college.setCollege("理学院");
        college.setOrgMode("UNIVERSITY");
        college.setSchoolId(1L);
        existing.add(college);
        Department clazz = new Department();
        clazz.setCollege("理学院");
        clazz.setMajor("本科");
        clazz.setClassName("大一");
        clazz.setOrgMode("UNIVERSITY");
        clazz.setSchoolId(1L);
        existing.add(clazz);
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(existing);

        int inserted = DepartmentStructureSeeder.ensureDefaultClassStructure(departmentMapper, 1L, "UNIVERSITY");

        assertEquals(0, inserted);
    }
}
