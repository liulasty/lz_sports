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

    @Select("SELECT COUNT(*) FROM athlete WHERE DATE_FORMAT(apply_time, '%Y%m') = #{month} AND athlete_state = '成功'")
    int getAthleteNumsByMonth(String month);

    @Select("SELECT COUNT(*) FROM athlete WHERE athlete_state = '成功'")
    Integer getAthleteTotal();

    @Select("SELECT COUNT(*) FROM athlete WHERE YEAR(agree_time) = #{year} AND MONTH(agree_time) = #{month}")
    int getAthleteNumByMonth(int year, int month);

    @Update("UPDATE athlete SET athlete_state = '不同意' WHERE user_id = #{userId}")
    void refusePlayer(Long userId);

    @Select("SELECT * FROM athlete WHERE user_id = #{userId}")
    Athlete selectByUserId(Long userId);
}
