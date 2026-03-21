package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.service.AdminStatsService;
import com.lz.vo.chart.EventStatsVO;
import com.lz.vo.chart.OverviewStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "管理端-数据统计", description = "系统各类数据统计接口")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "总览数据", description = "获取系统总体统计数据")
    public Result<OverviewStatsVO> getOverviewStats() {
        return Result.success(adminStatsService.getOverviewStats());
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "赛事维度统计", description = "获取各个赛事的统计数据")
    public Result<List<EventStatsVO>> getEventStats() {
        return Result.success(adminStatsService.getEventStats());
    }
}
