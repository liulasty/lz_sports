package com.lz.controller;

import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/notification", "/api/notifications"})
@RequiredArgsConstructor
@Tag(name = "站内信", description = "通知列表、已读标记、未读数")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通知")
    public Result<PageResult> list(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead) {
        return Result.success(notificationService.list(currentPage, pageSize, isRead));
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记单条已读")
    public Result<String> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success("标记成功");
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<String> markAllRead() {
        notificationService.markAllRead();
        return Result.success("全部已读");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读数量")
    public Result<Integer> unreadCount() {
        return Result.success(notificationService.unreadCount());
    }
}
