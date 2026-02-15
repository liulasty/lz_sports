package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Project Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT COUNT(*) FROM eventitem WHERE YEAR(createTime) = #{year} AND MONTH(createTime) = #{month}")
    int getProjectNumsByMonth(int year, int month);

    @Select("select COUNT(*) from eventitem")
    int getProjectTotal();
}
