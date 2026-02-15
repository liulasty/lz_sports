package com.lz.service;




import com.lz.pojo.dto.EventDTO;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.entity.Event;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.chart.TableData;
import com.lz.pojo.result.chart.TypeData;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

/**
 * 活动服务
 *
 * @author lz
 * @date 2023/12/06
 */
public interface EventService {
    /**
     * 添加事件
     *
     * @param eventDTO 事件 DTO
     *
     * @return {@code String}
     *
     * @throws SQLIntegrityConstraintViolationException SQLColiction 约束冲突异常
     */
    String addEvent(EventDTO eventDTO) throws SQLIntegrityConstraintViolationException;

    /**
     * 分页列表
     *
     * @param eventListDto 事件列表 DTO
     *
     * @return {@code PageResult}
     */
    PageResult list(EventListDto eventListDto);

    /**
     * 获取新十
     *
     * @return {@code TableData[]}
     */
    TableData[] getNewTen();


    /**
     * 获取事件 ID
     *
     * @param eventId 事件 ID
     *
     * @return {@code Event}
     */
    Event getEventId(Long eventId);

    /**
     * 删除事件
     *
     * @param eventId 事件 ID
     *
     * @return {@code String}
     *
     * @throws SQLException SQLException
     */
    String deleteEvent(String eventId) throws SQLException;

    /**
     * 更新
     *
     * @param eventId  事件 ID
     * @param eventDTO 事件 DTO
     */
    void update(String eventId, EventDTO eventDTO);

    /**
     * 获取事件类型
     *
     * @return {@code String[]}
     */
    List<Map<Long,String>> getEventType();

    /**
     * 按日期获取数据
     *
     * @param s s
     *
     * @return {@code TypeData}
     */
    TypeData getDataByDate(String s);
}