package com.lz.vo.chart;

import lombok.Data;
import java.util.Map;

@Data
public class OverviewStatsVO {
    private Long totalUsers;
    private Long totalEvents;
    private Map<String, Long> eventsByStatus;
    private Long totalRegistrations;
    private Long newUsersThisMonth;
}
