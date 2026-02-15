package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.common.result.PageResult;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.entity.Project;
import com.lz.vo.ProjectVO;

/**
 * Project Service Interface
 */
public interface ProjectService extends IService<Project> {

    /**
     * List Projects for Athlete (with status)
     */
    PageResult listByAthlete(EventListDTO listDto);

    /**
     * List Projects (Admin/General)
     */
    PageResult list(EventListDTO listDto);

    /**
     * Add Project
     */
    void add(ProjectDTO projectDTO);

    /**
     * Update Project
     */
    void update(ProjectDTO projectDTO, Long id);

    /**
     * Delete Project
     */
    void delete(Long id);

    /**
     * Get Project DTO
     */
    ProjectDTO getProject(Long id);
}
