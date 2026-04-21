package com.lz.controller;

import com.lz.common.annotation.RequireEventAdmin;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.service.ScoreService;
import com.lz.vo.ScoreImportResultVO;
import com.lz.vo.ScoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

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
            @Parameter(description = "赛事名称") @RequestParam(required = false) String eventName,
            @Parameter(description = "赛事ID") @RequestParam(required = false) Long eventId,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "仅已发布") @RequestParam(required = false) Boolean onlyPublished) {
        return Result.success(scoreService.list(currentPage, pageSize, eventName, eventId, itemId, onlyPublished));
    }

    @PostMapping("/upsert")
    @RequireEventAdmin
    @Operation(summary = "录入成绩", description = "仅审核通过的报名可录入，重复录入自动更新")
    public Result<String> upsert(@Valid @RequestBody ScoreUpsertDTO dto) {
        scoreService.upsertScore(dto);
        return Result.success("录入成功");
    }

    @PutMapping("/{scoreId}")
    @RequireEventAdmin
    @Operation(summary = "修改成绩", description = "已发布成绩不可修改")
    public Result<String> update(@PathVariable Long scoreId, @Valid @RequestBody ScoreUpsertDTO dto) {
        scoreService.updateScore(scoreId, dto);
        return Result.success("修改成功");
    }

    @GetMapping("/template/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "下载导入模板", description = "模板包含已通过报名数据，按项目分Sheet")
    public void template(@PathVariable Long eventId, HttpServletResponse response) {
        scoreService.downloadTemplate(eventId, response);
    }

    @PostMapping("/import/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "导入成绩", description = "管理员上传Excel文件批量导入赛事成绩")
    public Result<ScoreImportResultVO> importScores(
            @Parameter(description = "成绩Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "赛事ID") @PathVariable Long eventId) {
        return Result.success(scoreService.importScores(file, eventId));
    }

    @GetMapping("/export/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "导出成绩", description = "按项目分Sheet导出成绩表")
    public void exportScores(@PathVariable Long eventId, HttpServletResponse response) {
        scoreService.exportScores(eventId, response);
    }

    @GetMapping("/export-registration/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "导出报名名单", description = "导出报名名单Excel")
    public void exportRegistrations(@PathVariable Long eventId, HttpServletResponse response) {
        scoreService.exportRegistrations(eventId, response);
    }

    @PutMapping("/publish/{eventId}")
    @RequireEventAdmin
    @Operation(summary = "发布成绩", description = "管理员正式发布指定赛事的成绩")
    public Result<String> publishScores(@Parameter(description = "赛事ID") @PathVariable Long eventId) {
        scoreService.publishScores(eventId);
        return Result.success("成绩发布成功");
    }

    @GetMapping("/my")
    @Operation(summary = "我的成绩", description = "仅返回已发布成绩")
    public Result<List<ScoreVO>> myScores(@RequestParam(required = false) Long eventId) {
        return Result.success(scoreService.myScores(eventId));
    }

    @GetMapping("/public/{eventId}")
    @Operation(summary = "公开成绩榜", description = "仅返回已发布成绩，按项目分组")
    public Result<Map<String, List<ScoreVO>>> publicRanking(@PathVariable Long eventId) {
        return Result.success(scoreService.publicRanking(eventId));
    }
}
