package com.lz.vo;

import com.lz.common.enums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createTime;
}
