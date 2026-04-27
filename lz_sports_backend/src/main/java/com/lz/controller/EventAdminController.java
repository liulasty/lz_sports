package com.lz.controller;

import com.lz.common.annotation.RequireEventAdmin;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.service.AthleteService;
import com.lz.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-admin")
@RequiredArgsConstructor
@Tag(name = "赛事管理员", description = "赛事管理员专用的审核与统计接口")
public class EventAdminController {

    private final AthleteService athleteService;
    private final RegistrationService registrationService;

    @GetMapping("/{eventId}/athlete-applications")
    @RequireEventAdmin
    @Operation(summary = "按赛事查申请列表", description = "获取某赛事的运动员申请列表")
    public Result<PageResult> getAthleteApplications(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(athleteService.getAthleteApplicationsByEvent(eventId, status, keyword, page, size));
    }

    @PostMapping("/{eventId}/athlete-applications/{applicationId}/approve")
    @RequireEventAdmin
    @Operation(summary = "审核通过", description = "审核通过运动员资格")
    public Result<String> approveAthlete(
            @PathVariable Long eventId,
            @PathVariable Long applicationId) {
        athleteService.approveAthleteApplication(eventId, applicationId);
        return Result.success("审核通过");
    }

    @PostMapping("/{eventId}/athlete-applications/{applicationId}/reject")
    @RequireEventAdmin
    @Operation(summary = "审核拒绝", description = "审核拒绝运动员资格")
    public Result<String> rejectAthlete(
            @PathVariable Long eventId,
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        if ((reason == null || reason.isBlank()) && body != null) {
            String bodyReason = body.get("rejectReason");
            if (bodyReason != null && !bodyReason.isBlank()) {
                reason = bodyReason.trim();
            }
        }
        athleteService.rejectAthleteApplication(eventId, applicationId, reason);
        return Result.success("已拒绝");
    }

    @PostMapping("/{eventId}/athlete-applications/batch-approve")
    @RequireEventAdmin
    @Operation(summary = "批量审核", description = "批量审核通过运动员申请")
    public Result<String> batchApproveAthlete(
            @PathVariable Long eventId,
            @RequestBody List<Long> applicationIds) {
        athleteService.batchApproveAthleteApplications(eventId, applicationIds);
        return Result.success("批量审核通过");
    }

    @GetMapping("/{eventId}/registrations/stats")
    @RequireEventAdmin
    @Operation(summary = "报名统计", description = "获取赛事的报名统计数据")
    public Result<Map<String, Object>> getRegistrationStats(@PathVariable Long eventId) {
        return Result.success(registrationService.getRegistrationStatsByEvent(eventId));
    }
}
