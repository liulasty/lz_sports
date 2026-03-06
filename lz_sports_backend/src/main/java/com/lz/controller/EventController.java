package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.annotation.RequireEventAdmin;
import com.lz.common.enums.UserRole;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.Event;
import com.lz.service.EventService;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 赛事管理控制器
 * 提供赛事的增删改查及统计功能
 */
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Tag(name = "赛事管理", description = "赛事活动的增删改查与统计")
public class EventController {

    private final EventService eventService;

    /**
     * 发布新赛事
     * 管理员发布新的运动会或比赛活动
     * 注意：路径保持 /EventList 是为了兼容旧版前端
     */
    @PostMapping("/EventList")
    @RequireRole({UserRole.SCHOOL_ADMIN, UserRole.EVENT_ADMIN})
    @Operation(summary = "发布赛事", description = "管理员发布新的赛事活动")
    public Result<String> addEvent(@RequestBody EventDTO eventDTO) {
        // 如果包含图片URL列表，映射到添加图片逻辑
        if (eventDTO.getImageUrls() != null) {
            eventDTO.mapOssUrlToAddImage();
        }
        String msg = eventService.addEvent(eventDTO);
        return Result.success(msg);
    }

    /**
     * 分页查询赛事列表
     * 支持按名称、类型、日期筛选
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询赛事", description = "获取赛事列表，支持条件筛选")
    public Result<PageResult> list(
            @Parameter(description = "赛事名称") @RequestParam(required = false) String name,
            @Parameter(description = "赛事类型") @RequestParam(required = false) String type,
            @Parameter(description = "日期") @RequestParam(required = false) String date,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "5") int pageSize) {
        EventListDTO dto = new EventListDTO(name, type, date, currentPage, pageSize);
        return Result.success(eventService.list(dto));
    }

    /**
     * 获取最新发布的10条赛事
     * 用于首页展示最新动态
     */
    @GetMapping("/newTen")
    @Operation(summary = "最新赛事", description = "获取最新发布的10条赛事信息")
    public Result<List<TableData>> getNewTen() {
        return Result.success(eventService.getNewTen());
    }

    /**
     * 获取赛事详情
     * 根据ID查询赛事的详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "赛事详情", description = "根据ID获取赛事详细信息")
    public Result<Event> getEventById(@Parameter(description = "赛事ID") @PathVariable Long id) {
        return Result.success(eventService.getEventId(id));
    }

    /**
     * 删除赛事
     * 管理员删除指定赛事
     */
    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "删除赛事", description = "根据ID删除赛事")
    public Result<String> deleteEvent(@Parameter(description = "赛事ID") @PathVariable String id) {
        return Result.success(eventService.deleteEvent(id));
    }

    /**
     * 更新赛事信息
     * 管理员修改赛事内容
     */
    @PutMapping("/{id}")
    @RequireEventAdmin
    @Operation(summary = "更新赛事", description = "更新赛事的基本信息")
    public Result<String> updateEvent(
            @Parameter(description = "赛事ID") @PathVariable String id, 
            @RequestBody EventDTO eventDTO) {
        if (eventDTO.getImageUrls() != null) {
            eventDTO.mapOssUrlToAddImage();
        }
        eventService.update(id, eventDTO);
        return Result.success("更新成功");
    }

    /**
     * 修改赛事状态
     * 如发布、结束等状态变更
     */
    @PutMapping("/{id}/status")
    @RequireEventAdmin
    @Operation(summary = "修改状态", description = "更改赛事的当前状态")
    public Result<String> changeStatus(
            @Parameter(description = "赛事ID") @PathVariable Long id, 
            @Parameter(description = "新状态") @RequestParam String status) {
        eventService.changeStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 获取所有赛事类型
     * 返回ID和名称的映射列表
     */
    @GetMapping("/getEventType")
    @Operation(summary = "赛事类型", description = "获取所有可用的赛事类型列表")
    public Result<List<Map<Long, String>>> getEventType() {
        return Result.success(eventService.getEventType());
    }

    /**
     * 获取图表统计数据
     * 根据日期（年月）统计赛事数据
     */
    @GetMapping("/chart/{date}")
    @Operation(summary = "图表统计", description = "根据年月获取赛事统计数据")
    public Result<TypeData> getChartData(@Parameter(description = "日期(YYYY-MM)") @PathVariable String date) {
        return Result.success(eventService.getDataByDate(date));
    }

    /**
     * 获取赛事总数
     * 统计系统中的赛事总量
     */
    @GetMapping("/total")
    @Operation(summary = "赛事总数", description = "获取系统赛事总数量")
    public Result<Integer> getTotal() {
        return Result.success(eventService.getTotal());
    }
}
