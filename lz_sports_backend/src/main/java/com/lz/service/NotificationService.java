package com.lz.service;

import com.lz.common.enums.NotificationType;

public interface NotificationService {
    void create(Long userId, String title, String content, NotificationType type);
}
