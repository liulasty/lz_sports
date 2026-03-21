package com.lz.service;

import com.lz.vo.chart.EventStatsVO;
import com.lz.vo.chart.OverviewStatsVO;
import java.util.List;

public interface AdminStatsService {
    OverviewStatsVO getOverviewStats();
    List<EventStatsVO> getEventStats();
}
