package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.entity.Athlete;
import com.lz.mapper.AthleteMapper;
import com.lz.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        Athlete athlete = athleteMapper.selectByUserId(userId);
        
        if (athlete == null) {
            return Result.success(projectService.list(listDto));
        } else {
            return Result.success(projectService.listByAthlete(listDto));
        }
    }

    /**
     * 添加新项目
     * 管理员在指定赛事下创建新的比赛项目
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
    @Operation(summary = "添加项目", description = "管理员添加新的比赛项目")
    public Result<String> addProject(@RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        projectService.add(projectDTO);
        return Result.success("添加项目成功");
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

    /**
     * 删除项目
     * 管理员删除指定的比赛项目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "更新项目", description = "更新比赛项目信息")
    public Result<String> update(
            @Parameter(description = "项目ID") @PathVariable Long id, 
            @RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        projectService.update(projectDTO, id);
        return Result.success("更新成功");
    }
}
