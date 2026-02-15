package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.entity.Athlete;
import com.lz.mapper.AthleteMapper;
import com.lz.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Project Controller
 */
@Slf4j
@RestController
@RequestMapping("sports/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AthleteMapper athleteMapper;

    /**
     * List Projects
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String event,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {
        log.info("分页查询:{},{},{}", currentPage, pageSize, event);
        
        // Map 'event' param to 'type' field in DTO as discussed
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
     * Add Project
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> addProject(@RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        projectService.add(projectDTO);
        return Result.success("添加项目成功");
    }

    /**
     * Get Project
     */
    @GetMapping("/{id}")
    public Result<ProjectDTO> get(@PathVariable Long id) {
        return Result.success(projectService.getProject(id));
    }

    /**
     * Delete Project
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success("删除成功");
    }
    
    /**
     * Update Project
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> update(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getImageUrls() != null) {
            projectDTO.mapOssUrlToAddImage();
        }
        projectService.update(projectDTO, id);
        return Result.success("更新成功");
    }
}
