package com.lz.Dao;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/22:53
 * @Description:
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.entity.Event;
import com.lz.pojo.entity.Project;
import com.lz.pojo.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lz
 */
@Mapper
public interface ProjectDao extends BaseMapper<Project> {

    /**
     * 选择项目
     *
     * @param eventListDto 事件列表 DTO
     *
     * @return {@code List<ProjectVO>}
     */
    List<ProjectVO> selectProject(EventListDto eventListDto);

    /**
     * 选择项目总计
     *
     * @param listDto 列出 DTO
     *
     * @return long
     */
    long selectProjectTotal(EventListDto listDto);


    /**
     * 添加项目
     *
     * @param project 项目
     *
     * @return {@code Long}
     */
    Long addProject(Project project);

    /**
     * 按月获取项目编号
     *
     * @param year  年
     * @param month 月
     *
     * @return int
     */
    @Select("SELECT COUNT(*) FROM lz_sports.eventitem WHERE YEAR(createTime) = " +
            "#{year}" +
            " AND MONTH(createTime) = #{month}")
    int getProjectNumsByMonth(int year, int month);

    /**
     * 获取项目总计
     *
     * @return int
     */
    @Select("select COUNT(*) from lz_sports.eventitem")
    int getProjectTotal();
}