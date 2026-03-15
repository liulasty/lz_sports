package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Project Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT COUNT(*) FROM event_item WHERE YEAR(create_time) = #{year} AND MONTH(create_time) = #{month}")
    int getProjectNumsByMonth(int year, int month);

    @Select("select COUNT(*) from event_item")
    int getProjectTotal();

    @Update("UPDATE event_item SET current_count = current_count + 1 WHERE id = #{projectId} AND current_count < #{maxAttendance}")
    int incrementAttendance(Long projectId, Integer maxAttendance);

    @Update("UPDATE event_item SET current_count = current_count - 1 WHERE id = #{projectId} AND current_count > 0")
    int decrementAttendance(Long projectId);
}
