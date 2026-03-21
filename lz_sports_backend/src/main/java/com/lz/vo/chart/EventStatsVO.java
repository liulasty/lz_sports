package com.lz.vo.chart;

import lombok.Data;

@Data
public class EventStatsVO {
    private Long eventId;
    private String eventName;
    private Long totalRegistrations;
    private Long approvedRegistrations;
    private Long publishedProjects;
}
