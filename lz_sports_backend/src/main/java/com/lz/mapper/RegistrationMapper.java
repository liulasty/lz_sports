package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 报名 Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("select COUNT(DISTINCT AthleteID) from Registration where RegistrationStatus = '通过'")
    Integer getRegistrationPlayerTotal();
}
