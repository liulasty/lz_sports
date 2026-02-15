package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Athlete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Athlete Mapper
 */
@Mapper
public interface AthleteMapper extends BaseMapper<Athlete> {

    @Select("SELECT COUNT(*) FROM athlete WHERE DATE_FORMAT(applyTime, '%Y%m') = #{month} AND AthleteState = '成功'")
    int getAthleteNumsByMonth(String month);

    @Select("SELECT COUNT(*) FROM athlete WHERE AthleteState = '成功'")
    Integer getAthleteTotal();

    @Select("SELECT COUNT(*) FROM athlete WHERE YEAR(agreeTime) = #{year} AND MONTH(agreeTime) = #{month}")
    int getAthleteNumByMonth(int year, int month);

    @Update("UPDATE athlete SET AthleteState = '不同意' WHERE UserID = #{userId}")
    void refusePlayer(Long userId);

    @Select("SELECT * FROM athlete WHERE UserID = #{userId}")
    Athlete selectByUserId(Long userId);
}
