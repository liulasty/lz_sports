package com.lz.controller;

import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.dto.EventListDTO;
import com.lz.entity.Event;
import com.lz.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户公开读接口（无需登录），供首页「成绩公示」等场景使用。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "门户公开", description = "无需登录的只读接口")
public class PublicPortalController {

    private final EventService eventService;

    @GetMapping("/events")
    @Operation(summary = "分页查询赛事（公开）", description = "与赛事大厅同源的分页列表，匿名可访问")
    public Result<PageResult> listEvents(
            @Parameter(description = "赛事名称") @RequestParam(required = false) String name,
            @Parameter(description = "赛事类型") @RequestParam(required = false) String type,
            @Parameter(description = "日期") @RequestParam(required = false) String date,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") int pageSize) {
        EventListDTO dto = new EventListDTO(name, type, date, currentPage, pageSize);
        return Result.success(eventService.list(dto));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "赛事详情（公开）", description = "根据 ID 获取赛事信息，匿名可访问")
    public Result<Event> getEvent(@Parameter(description = "赛事ID") @PathVariable Long id) {
        return Result.success(eventService.getEventId(id));
    }
}
