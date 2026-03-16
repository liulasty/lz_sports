package com.lz.service;

import com.lz.common.enums.NotificationType;
import com.lz.common.result.PageResult;
import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String title, String content, NotificationType type);

    void batchSendNotification(List<Long> userIds, String title, String content, NotificationType type);

    PageResult list(Integer currentPage, Integer pageSize, Boolean isRead);

    void markRead(Long notificationId);

    void markAllRead();

    Integer unreadCount();

    default void create(Long userId, String title, String content, NotificationType type) {
        sendNotification(userId, title, content, type);
    }
}
