package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Event;
import com.lz.vo.chart.TypeData;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Event Mapper
 */
@Mapper
public interface EventMapper extends BaseMapper<Event> {

    @MapKey("eventId")
    @Select("SELECT EventID, EventName FROM event")
    List<Map<Long, String>> selectEventName();

    @Select("SELECT COUNT(*) FROM event WHERE YEAR(RegistrationStart) = #{year} AND MONTH(RegistrationStart) = #{month}")
    int getEventNumsByMonth(int year, int month);

    @Select("SELECT count(*) from event")
    int getEventTotal();
    
    // Complex query for stats - simplified or ported from XML
    @Select("""
        SELECT
            SUM(CASE WHEN eligibility = '线上报名' THEN 1 ELSE 0 END) AS online,
            SUM(CASE WHEN eligibility = '单位报名' THEN 1 ELSE 0 END) AS 'group',
            SUM(CASE WHEN eligibility = '线下报名' THEN 1 ELSE 0 END) AS offline,
            SUM(CASE WHEN eligibility NOT IN ('线上报名', '单位报名', '线下报名') THEN 1 ELSE 0 END) AS other
        FROM event
        WHERE DATE_FORMAT(RegistrationStart, '%Y%m') = #{date}
    """)
    TypeData selectNumsByDate(String date);
}
