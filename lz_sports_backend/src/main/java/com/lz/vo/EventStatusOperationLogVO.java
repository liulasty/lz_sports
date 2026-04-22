package com.lz.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventStatusOperationLogVO {
    private Long id;
    private Long eventId;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorName;
    private String operationType;
    private String triggerSource;
    private String reason;
    private LocalDateTime createTime;
}
