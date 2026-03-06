package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.entity.Athlete;
import com.lz.mapper.AthleteMapper;
import com.lz.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 报名管理控制器
 * 处理赛事报名的申请、审核、查询及导出
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Tag(name = "报名管理", description = "赛事报名、审核与导出")
public class RegistrationController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationService registrationService;
    private final AthleteMapper athleteMapper;

    /**
     * 分页查询报名列表
     * 管理员可查看所有报名，运动员仅查看自己的报名记录
     */
    @GetMapping("/page")
    @Operation(summary = "查询报名列表", description = "分页查询报名记录，支持多条件筛选")
    public Result<PageResult> list(
            @Parameter(description = "运动员姓名") @RequestParam(required = false) String name,
            @Parameter(description = "审核状态") @RequestParam(required = false) String status,
            @Parameter(description = "报名日期") @RequestParam(required = false) String date,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "5") int pageSize) {
        log.info("查询报名列表: {}, {}, {}", name, status, date);
        
        Date queryDate = parseDate(date);
        
        Long userId = BaseContext.getCurrentId();
        Athlete athlete = athleteMapper.selectByUserId(userId);
        
        if (athlete == null) {
            return Result.success(registrationService.list(currentPage, pageSize, name, status, queryDate));
        } else {
            return Result.success(registrationService.listByAthlete(currentPage, pageSize, name, status, queryDate, athlete.getAthleteId()));
        }
    }

    /**
     * 查询报名详情
     * 获取单条报名记录的详细信息及关联运动员信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "报名详情", description = "获取报名详细信息")
    public Result<RegistrationAndAthleteDTO> getDetail(@Parameter(description = "报名ID") @PathVariable Long id) {
        return Result.success(registrationService.getDetail(id));
    }
    
    /**
     * 删除/取消报名
     * 删除指定的报名记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除报名", description = "删除或取消报名记录")
    public Result<String> delete(@Parameter(description = "报名ID") @PathVariable Long id) {
        registrationService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 申请报名
     * 运动员申请参加指定项目
     */
    @PostMapping("/apply/{projectId}")
    @Operation(summary = "申请报名", description = "运动员申请参加比赛项目")
    public Result<String> apply(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        registrationService.add(projectId);
        return Result.success("报名申请已提交");
    }
    
    /**
     * 同意报名
     * 管理员审核通过报名申请
     */
    @PutMapping("/attend/{id}")
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
    @Operation(summary = "同意报名", description = "管理员审核通过报名申请")
    public Result<String> attend(@Parameter(description = "报名ID") @PathVariable Long id) {
        registrationService.approve(id);
        return Result.success("已通过报名");
    }

    /**
     * 拒绝报名
     * 管理员拒绝报名申请
     */
    @PutMapping("/refuse/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "拒绝报名", description = "管理员拒绝报名申请")
    public Result<String> refuse(@Parameter(description = "报名ID") @PathVariable Long id) {
        registrationService.refuse(id);
        return Result.success("已拒绝报名");
    }

    /**
     * 导出报名名单
     * 导出指定赛事的报名人员名单Excel
     */
    @GetMapping("/export/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "导出名单", description = "导出指定赛事的报名名单Excel")
    public void export(
            @Parameter(description = "赛事ID") @PathVariable Long eventId, 
            jakarta.servlet.http.HttpServletResponse response) {
        registrationService.export(eventId, response);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "null".equals(dateStr)) return null;
        try {
            if (dateStr.contains("T")) {
                 return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateStr);
            }
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            log.warn("Date parse error: {}", dateStr);
            return null;
        }
    }
}
