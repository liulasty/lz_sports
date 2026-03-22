package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.Result;
import com.lz.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@Tag(name = "赛事管理员分配", description = "管理赛事的管理员分配")
public class AdminEventController {

    private final EventService eventService;

    @PostMapping("/{eventId}/admins")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "指定管理员", description = "为赛事指定管理员")
    public Result<String> addAdmins(
            @PathVariable Long eventId,
            @RequestBody List<Long> userIds) {
        eventService.addEventAdmins(eventId, userIds);
        return Result.success("指定管理员成功");
    }

    @DeleteMapping("/{eventId}/admins/{userId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "移除管理员", description = "移除赛事的管理员")
    public Result<String> removeAdmin(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        eventService.removeEventAdmin(eventId, userId);
        return Result.success("移除管理员成功");
    }

    @GetMapping("/{eventId}/admins")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "管理员列表", description = "获取赛事的管理员列表")
    public Result<List<com.lz.entity.User>> getAdmins(@PathVariable Long eventId) {
        return Result.success(eventService.getEventAdmins(eventId));
    }

    @PostMapping("/{eventId}/withdraw")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "赛事撤回", description = "撤回发布的赛事")
    public Result<String> withdrawEvent(@PathVariable Long eventId) {
        eventService.changeStatus(eventId, "DRAFT");
        return Result.success("赛事已撤回");
    }
}
