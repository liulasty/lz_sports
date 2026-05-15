package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.PageResult;
import com.lz.service.EligibilityService;
import com.lz.common.result.Result;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.dto.EligibilityPreviewVO;
import com.lz.entity.Athlete;
import com.lz.entity.Project;
import com.lz.mapper.AthleteMapper;
import com.lz.service.ProjectService;
import com.lz.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 项目管理控制器
 * 负责比赛项目（如100米、跳高）的增删改查
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "具体比赛项目的管理接口")
public class ProjectController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final AthleteMapper athleteMapper;
    private final EligibilityService eligibilityService;

    /**
     * 分页查询项目列表
     * 支持按名称、所属赛事、日期筛选。若用户是运动员，仅返回符合其性别的项目。
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询项目", description = "查询比赛项目列表，运动员只能看到符合条件的项目")
    public Result<PageResult> list(
            @Parameter(description = "项目名称") @RequestParam(required = false) String name,
            @Parameter(description = "所属赛事") @RequestParam(required = false) String event,
            @Parameter(description = "日期") @RequestParam(required = false) String date,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "5") int pageSize) {
        log.info("分页查询:{},{},{}", currentPage, pageSize, event);
        
        // 映射参数到DTO
        EventListDTO listDto = new EventListDTO(name, event, date, currentPage, pageSize);
        
        Long userId = BaseContext.getCurrentId();
        // If user is logged in, use listByAthlete to check registration status
        if (userId != null) {
            return Result.success(projectService.listByAthlete(listDto));
        } else {
            return Result.success(projectService.list(listDto));
        }
    }

    /**
     * 添加新项目
     * 管理员在指定赛事下创建新的比赛项目
     */
    @PostMapping
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "添加项目", description = "管理员添加新的比赛项目")
    public Result<Long> addProject(@Valid @RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        Long projectId = projectService.add(projectDTO);
        return Result.success(projectId);
    }

    /**
     * 获取项目详情
     * 根据ID查询项目的详细配置信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "项目详情", description = "根据ID获取项目详细信息")
    public Result<ProjectDTO> get(@Parameter(description = "项目ID") @PathVariable Long id) {
        return Result.success(projectService.getProject(id));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "赛事项目列表", description = "根据赛事ID获取项目列表")
    public Result<List<ProjectVO>> getByEvent(@Parameter(description = "赛事ID") @PathVariable Long eventId) {
        return Result.success(projectService.listByEventId(eventId));
    }

    /**
     * 删除项目
     * 管理员删除指定的比赛项目
     */
    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "删除项目", description = "根据ID删除比赛项目")
    public Result<String> delete(@Parameter(description = "项目ID") @PathVariable Long id) {
        projectService.delete(id);
        return Result.success("删除成功");
    }
    
    /**
     * 更新项目信息
     * 管理员修改项目配置
     */
    @PutMapping("/{id}")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "更新项目", description = "更新比赛项目信息")
    public Result<String> update(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Valid @RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        projectService.update(projectDTO, id);
        return Result.success("更新成功");
    }

    /**
     * 当前用户资格预览
     */
    @GetMapping("/{id}/eligibility/preview")
    @Operation(summary = "资格预览", description = "当前登录用户是否有资格报名该项目")
    public Result<EligibilityPreviewVO> preview(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.success(new EligibilityPreviewVO(id, false, "用户未登录"));
        }
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.success(new EligibilityPreviewVO(id, false, "项目不存在"));
        }
        com.lz.eligibility.engine.EligibilityResult result = eligibilityService.check(userId, project.getEventId(), id);
        return Result.success(new EligibilityPreviewVO(id, result.isPassed(), result.getReason()));
    }

    /**
     * 批量资格检查（项目列表灰显用）
     */
    @PostMapping("/eligibility/batch-check")
    @Operation(summary = "批量资格检查", description = "当前用户在多个项目上的资格状态")
    public Result<List<EligibilityPreviewVO>> batchCheck(@RequestBody List<Long> itemIds) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null || itemIds == null || itemIds.isEmpty()) {
            return Result.success(List.of());
        }
        // 从第一个项目获取 eventId（同一页面列表的 eventId 相同）
        Project first = projectService.getById(itemIds.get(0));
        if (first == null) return Result.success(List.of());
        List<EligibilityPreviewVO> results = eligibilityService.batchCheck(userId, first.getEventId(), itemIds);
        return Result.success(results);
    }
}
