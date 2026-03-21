package com.lz.controller;

import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.UserQueryDTO;
import com.lz.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "管理端-用户管理", description = "超级管理员的用户管理接口")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "获取用户列表")
    public Result<PageResult> getUsers(UserQueryDTO queryDTO) {
        return Result.success(adminUserService.getUsers(queryDTO));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "修改用户角色")
    public Result<Void> changeRole(@PathVariable Long id, @RequestParam String role) {
        adminUserService.changeRole(id, role);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "禁用用户")
    public Result<Void> disableUser(@PathVariable Long id) {
        adminUserService.disableUser(id);
        return Result.success();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "启用用户")
    public Result<Void> enableUser(@PathVariable Long id) {
        adminUserService.enableUser(id);
        return Result.success();
    }
}
