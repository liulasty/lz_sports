package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.dto.RegistrationDTO;
import com.lz.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 报名 Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("select COUNT(DISTINCT AthleteID) from Registration where RegistrationStatus = '通过'")
    Integer getRegistrationPlayerTotal();

    /**
     * 分页查询报名列表 (联表查询优化)
     */
    IPage<RegistrationDTO> selectRegistrationPage(Page<RegistrationDTO> page,
                                                  @Param("name") String name,
                                                  @Param("status") String status,
                                                  @Param("date") Date date,
                                                  @Param("athleteId") Long athleteId);

    /**
     * 查询报名列表 (导出用)
     */
    List<RegistrationDTO> selectRegistrationList(@Param("eventId") Long eventId);
}
