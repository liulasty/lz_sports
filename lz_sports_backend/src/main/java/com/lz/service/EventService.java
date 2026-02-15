package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.common.result.PageResult;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.Event;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;

import java.util.List;
import java.util.Map;

/**
 * Event Service Interface
 */
public interface EventService extends IService<Event> {

    /**
     * Add Event
     */
    String addEvent(EventDTO eventDTO);

    /**
     * List Events with pagination
     */
    PageResult list(EventListDTO eventListDto);

    /**
     * Get Newest 10 Events
     */
    List<TableData> getNewTen();

    /**
     * Get Event by ID
     */
    Event getEventId(Long eventId);

    /**
     * Delete Event
     */
    String deleteEvent(String eventId);

    /**
     * Update Event
     */
    void update(String eventId, EventDTO eventDTO);

    /**
     * Get Event Types
     */
    List<Map<Long, String>> getEventType();

    /**
     * Get Data by Date for Charts
     */
    TypeData getDataByDate(String date);

    /**
     * Get Total Events count
     */
    int getTotal();
}
