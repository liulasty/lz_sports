package com.lz.Dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.pojo.entity.Event;
import com.lz.pojo.result.chart.TypeData;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author lz
 */
@Mapper
@Transactional
public interface EventDao extends BaseMapper<Event> {
    /**
     * INSERT 事件
     *
     * @param event 事件
     */
    void insertEvent(Event event);


    /**
     * 选择事件名称
     *
     * @return {@code List<Map<Long,String>>}
     */
    @MapKey("eventId")
    List<Map<Long,String>> selectEventName();

    /**
     * 按日期选择数字
     *
     * @return {@code TypeData}
     */
   TypeData selectNumsByDate(String date);

    /**
     * 按月获取事件编号
     *
     * @param year  年
     * @param month 月
     *
     * @return int
     */
    @Select("SELECT COUNT(*) FROM lz_sports.athlete WHERE YEAR(agreeTime) = " +
            "#{year}" +
            " AND MONTH(agreeTime) = #{month}")
    int getEventNumsByMonth(int year, int month);

    /**
     * 获取事件总数
     *
     * @return int
     */
    @Select("SELECT count(*) from lz_sports.event")
    int getEventTotal();
}