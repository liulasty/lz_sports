package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.entity.Athlete;
import com.lz.mapper.AthleteMapper;
import com.lz.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 报名控制器
 */
@Slf4j
@RestController
@RequestMapping("sports/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final AthleteMapper athleteMapper;

    /**
     * 分页查询报名列表
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {
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
     */
    @GetMapping("/{id}")
    public Result<RegistrationAndAthleteDTO> getDetail(@PathVariable Long id) {
        return Result.success(registrationService.getDetail(id));
    }
    
    /**
     * 删除报名
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        registrationService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 申请报名 (User submits application for a project)
     * Note: Old code didn't have explicit "apply" endpoint in RegistrationController?
     * Checking old code... old code might have handled this in ProjectController or had a separate logic.
     * But usually "Join Project" is an action.
     * Let's add a POST endpoint for creating registration.
     */
    @PostMapping("/apply/{projectId}")
    public Result<String> apply(@PathVariable Long projectId) {
        registrationService.add(projectId);
        return Result.success("报名申请已提交");
    }
    
    /**
     * 同意报名 (Admin)
     */
    @PutMapping("/attend/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> attend(@PathVariable Long id) {
        registrationService.approve(id);
        return Result.success("已通过报名");
    }

    /**
     * 拒绝报名 (Admin)
     */
    @PutMapping("/refuse/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> refuse(@PathVariable Long id) {
        registrationService.refuse(id);
        return Result.success("已拒绝报名");
    }

    /**
     * 导出报名名单
     */
    @GetMapping("/export/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void export(@PathVariable Long eventId, jakarta.servlet.http.HttpServletResponse response) {
        registrationService.export(eventId, response);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "null".equals(dateStr)) return null;
        // Try parsing ISO date or simple date
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
