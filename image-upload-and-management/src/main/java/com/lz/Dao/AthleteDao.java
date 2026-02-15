package com.lz.Dao;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/14:43
 * @Description:
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.pojo.entity.Athlete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lz
 */
@Mapper
@Transactional
public interface AthleteDao extends BaseMapper<Athlete> {

    /**
     * 按月获取运动员数据
     *
     * @param month 月
     *
     * @return int
     */
    @Select("SELECT COUNT(*) FROM lz_sports.athlete" +
            "  WHERE DATE_FORMAT(applyTime, '%Y%m') = #{month} and " +
            "AthleteState = '成功'")
    int getAthleteNumsByMonth(String month);

    /**
     * 获取运动员总数
     *
     * @return {@code Integer}
     */
    @Select("select COUNT(*) from lz_sports.athlete where AthleteState = '成功'")
    Integer getAthleteTotal();

    /**
     * 按月获取运动员数据
     *
     * @param year  年
     * @param month 月
     *
     * @return int
     */
    @Select("SELECT COUNT(*) FROM lz_sports.athlete WHERE YEAR(agreeTime) = " +
            "#{year}" +
            " AND MONTH(agreeTime) = #{month}")
    int getAthleteNumByMonth(int year, int month);

    /**
     * 按用户ID选择
     *
     * @param id 编号
     *
     * @return {@code Athlete}
     */
    Athlete selectByUserId(Integer id);

    /**
     * 拒绝申请
     *
     * @param id 编号
     */
    void refusePlayer(Integer id);

    /**
     * 按用户 ID 删除
     *
     * @param id 编号
     */
    void deleteByUserId(Integer id);
}