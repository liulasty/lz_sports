package com.lz.controller;

import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.Event;
import com.lz.service.EventService;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Event Controller
 */
@RestController
@RequestMapping("/sports/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Add Event
     * Note: Mapped to /EventList to match old frontend, but standard REST would be POST /
     */
    @PostMapping("/EventList")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> addEvent(@RequestBody EventDTO eventDTO) {
        // Map OSS URLs logic if needed, but service handles addImage array
        if (eventDTO.getImageUrls() != null) {
            eventDTO.mapOssUrlToAddImage();
        }
        String msg = eventService.addEvent(eventDTO);
        return Result.success(msg);
    }

    /**
     * Get Event List (Pagination)
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {
        EventListDTO dto = new EventListDTO(name, type, date, currentPage, pageSize);
        return Result.success(eventService.list(dto));
    }

    /**
     * Get Newest 10 Events
     */
    @GetMapping("/newTen")
    public Result<List<TableData>> getNewTen() {
        return Result.success(eventService.getNewTen());
    }

    /**
     * Get Event By ID
     */
    @GetMapping("/{id}")
    public Result<Event> getEventById(@PathVariable Long id) {
        return Result.success(eventService.getEventId(id));
    }

    /**
     * Delete Event
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> deleteEvent(@PathVariable String id) {
        return Result.success(eventService.deleteEvent(id));
    }

    /**
     * Update Event
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> updateEvent(@PathVariable String id, @RequestBody EventDTO eventDTO) {
        if (eventDTO.getImageUrls() != null) {
            eventDTO.mapOssUrlToAddImage();
        }
        eventService.update(id, eventDTO);
        return Result.success("更新成功");
    }

    /**
     * Change Event Status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> changeStatus(@PathVariable Long id, @RequestParam String status) {
        eventService.changeStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * Get Event Types (Name/ID map)
     */
    @GetMapping("/getEventType")
    public Result<List<Map<Long, String>>> getEventType() {
        return Result.success(eventService.getEventType());
    }

    /**
     * Get Chart Data by Date (YYYYMM)
     */
    @GetMapping("/chart/{date}")
    public Result<TypeData> getChartData(@PathVariable String date) {
        return Result.success(eventService.getDataByDate(date));
    }

    /**
     * Get Total Events Count
     */
    @GetMapping("/total")
    public Result<Integer> getTotal() {
        return Result.success(eventService.getTotal());
    }
}
