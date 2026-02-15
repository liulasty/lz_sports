package com.lz.service;

import com.lz.Exception.MyException;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.dto.JoinProjectDTO;
import com.lz.pojo.dto.ProjectDTO;
import com.lz.pojo.result.PageResult;

import java.sql.SQLException;


/**
 * 项目服务
 *
 * @author lz
 * @date 2023/12/03
 */
public interface ProjectService {

    /**
     * 运动员名单
     *
     * @param listDto 列出 DTO
     *
     * @return {@code PageResult}
     */
    PageResult listByAthlete(EventListDto listDto);
    /**
     * 列表
     * 项目列表
     *
     * @param listDto 列出 DTO
     *
     * @return {@code PageResult}
     */
    PageResult list(EventListDto listDto);

    /**
     * 添加项目
     *
     * @param projectDTO 项目 DTO
     */
    void add(ProjectDTO projectDTO);

    /**
     * 编辑
     *
     * @param projectDTO 项目 DTO
     */
    void update(ProjectDTO projectDTO,Long id);

    /**
     * 删除
     *
     * @param id 编号
     */
    void delete(Long id) throws SQLException;

    /**
     * 获取项目
     *
     * @param id 编号
     *
     * @return {@code ProjectDTO}
     */
    ProjectDTO getProject(Long id);

    /**
     * 加入项目
     *
     * @param joinProjectDTO 加入 DTO 项目
     */
    void join(JoinProjectDTO joinProjectDTO) throws MyException;
}