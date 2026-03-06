package com.lz.controller;

import com.lz.common.annotation.RequireEventAdmin;
import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 成绩管理控制器
 * 负责赛事成绩的录入、导入、发布与查询
 */
@Slf4j
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
@Tag(name = "成绩管理", description = "成绩录入、导入、发布与查询接口")
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * 分页查询成绩列表
     * 获取所有已录入或已发布的成绩信息
     */
    @GetMapping("/page")
    @Operation(summary = "查询成绩列表", description = "分页查询成绩记录，支持按赛事名称筛选")
    public Result<PageResult> list(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "赛事名称") @RequestParam(required = false) String eventName) {
        return Result.success(scoreService.list(currentPage, pageSize, eventName));
    }

    /**
     * 导入成绩
     * 管理员通过Excel文件批量导入指定赛事的成绩
     */
    @PostMapping("/import/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "导入成绩", description = "管理员上传Excel文件批量导入赛事成绩")
    public Result<String> importScores(
            @Parameter(description = "成绩Excel文件") @RequestParam("file") MultipartFile file, 
            @Parameter(description = "赛事ID") @PathVariable Long eventId) {
        scoreService.importScores(file, eventId);
        return Result.success("成绩导入成功");
    }

    /**
     * 发布成绩
     * 管理员将录入的成绩正式对外发布，运动员方可查看
     */
    @PutMapping("/publish/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "发布成绩", description = "管理员正式发布指定赛事的成绩")
    public Result<String> publishScores(@Parameter(description = "赛事ID") @PathVariable Long eventId) {
        scoreService.publishScores(eventId);
        return Result.success("成绩发布成功");
    }
}
