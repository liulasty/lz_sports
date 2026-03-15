package com.lz.service.impl;

import com.lz.common.enums.NotificationType;
import com.lz.entity.Notification;
import com.lz.mapper.NotificationMapper;
import com.lz.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Async
    public void create(Long userId, String title, String content, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type(type)
                .isRead(false)
                .build();
        notification.initTime();
        notificationMapper.insert(notification);
    }
}
