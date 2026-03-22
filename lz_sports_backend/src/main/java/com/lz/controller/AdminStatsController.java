package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.Result;
import com.lz.service.AdminStatsService;
import com.lz.vo.chart.EventStatsVO;
import com.lz.vo.chart.OverviewStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "管理端-数据统计", description = "超级管理员的数据统计接口")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "获取总览数据", description = "获取系统级数据统计概览")
    public Result<OverviewStatsVO> getOverviewStats() {
        return Result.success(adminStatsService.getOverviewStats());
    }

    @GetMapping("/events")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "获取赛事统计", description = "获取各赛事的报名和成绩统计")
    public Result<List<EventStatsVO>> getEventStats() {
        return Result.success(adminStatsService.getEventStats());
    }
}
