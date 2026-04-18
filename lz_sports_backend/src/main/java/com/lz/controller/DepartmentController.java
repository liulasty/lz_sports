package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.Result;
import com.lz.service.DepartmentService;
import com.lz.service.SchoolConfigService;
import com.lz.vo.DepartmentTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.lz.entity.Department;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
@Tag(name = "组织架构管理", description = "部门组织架构树相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final SchoolConfigService schoolConfigService;

    @GetMapping("/tree")
    @Operation(summary = "获取组织架构选项", description = "返回所有部门的级联结构数据")
    public Result<List<DepartmentTreeVO>> tree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @PostMapping
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "新增部门", description = "新增组织架构节点")
    public Result<Boolean> add(@RequestBody Department department) {
        String currentMode = schoolConfigService.getCurrentOrgMode();
        department.setOrgMode(currentMode);
        if ("K12".equals(currentMode)) {
            department.setCollege(null);
            department.setMajor(null);
        } else {
            department.setGrade(null);
        }
        return Result.success(departmentService.save(department));
    }

    @PutMapping
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "修改部门", description = "修改组织架构节点")
    public Result<Boolean> update(@RequestBody Department department) {
        String currentMode = schoolConfigService.getCurrentOrgMode();
        department.setOrgMode(currentMode);
        if ("K12".equals(currentMode)) {
            department.setCollege(null);
            department.setMajor(null);
        } else {
            department.setGrade(null);
        }
        return Result.success(departmentService.updateById(department));
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "删除部门", description = "删除组织架构节点")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(departmentService.removeById(id));
    }
}