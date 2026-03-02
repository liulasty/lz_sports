package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lz.common.result.PageResult;
import com.lz.vo.ScoreVO;

/**
 * Score Controller
 */
@Slf4j
@RestController
@RequestMapping("/sports/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * List Scores
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) String eventName) {
        return Result.success(scoreService.list(currentPage, pageSize, eventName));
    }

    /**
     * Import Scores
     */
    @PostMapping("/import/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> importScores(@RequestParam("file") MultipartFile file, @PathVariable Long eventId) {
        scoreService.importScores(file, eventId);
        return Result.success("成绩导入成功");
    }

    /**
     * Publish Scores
     */
    @PutMapping("/publish/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> publishScores(@PathVariable Long eventId) {
        scoreService.publishScores(eventId);
        return Result.success("成绩发布成功");
    }
}
