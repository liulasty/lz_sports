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

    @Select("SELECT COUNT(*) FROM registration WHERE user_id = #{userId} AND event_id = #{eventId} AND status <> 'CANCELLED'")
    int countActiveByUserAndEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Select("""
        SELECT p.name FROM registration r
        JOIN event_item p ON r.item_id = p.id
        WHERE r.user_id = #{userId}
          AND r.event_id = #{eventId}
          AND r.status <> 'CANCELLED'
          AND p.start_time IS NOT NULL
          AND p.end_time IS NOT NULL
          AND p.start_time < #{newEnd}
          AND p.end_time > #{newStart}
        LIMIT 1
    """)
    String findConflictItemName(@Param("userId") Long userId,
                                @Param("eventId") Long eventId,
                                @Param("newStart") Date newStart,
                                @Param("newEnd") Date newEnd);

    @Select("""
        SELECT event_id as eventId, 
               COUNT(*) as total, 
               SUM(CASE WHEN status IN ('APPROVED', 'CONFIRMED') THEN 1 ELSE 0 END) as approved 
        FROM registration 
        GROUP BY event_id
    """)
    List<java.util.Map<String, Object>> countRegistrationsGroupByEvent();
}
