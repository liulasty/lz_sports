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
    @Select("SELECT id as eventId, name as eventName FROM event")
    List<Map<Long, String>> selectEventName();

    @Select("SELECT COUNT(*) FROM event WHERE YEAR(reg_start_time) = #{year} AND MONTH(reg_start_time) = #{month}")
    int getEventNumsByMonth(int year, int month);
    
    @Select("SELECT COUNT(*) FROM event")
    int getEventTotal();

    // Complex query for stats - simplified or ported from XML
    @Select("""
        SELECT
            SUM(CASE WHEN description LIKE '%线上%' THEN 1 ELSE 0 END) AS online,
            SUM(CASE WHEN description LIKE '%单位%' THEN 1 ELSE 0 END) AS 'group',
            SUM(CASE WHEN description LIKE '%线下%' THEN 1 ELSE 0 END) AS offline,
            SUM(CASE WHEN description NOT LIKE '%线上%' AND description NOT LIKE '%单位%' AND description NOT LIKE '%线下%' THEN 1 ELSE 0 END) AS other
        FROM event
        WHERE DATE_FORMAT(reg_start_time, '%Y%m') = #{date}
    """)
    TypeData selectNumsByDate(String date);
}
