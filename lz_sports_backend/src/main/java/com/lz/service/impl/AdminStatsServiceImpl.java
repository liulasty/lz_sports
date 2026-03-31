package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.entity.Event;
import com.lz.mapper.EventMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.AdminStatsService;
import com.lz.vo.chart.EventStatsVO;
import com.lz.vo.chart.OverviewStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserMapper userMapper;
    private final EventMapper eventMapper;
    private final RegistrationMapper registrationMapper;
    private final ScoreMapper scoreMapper;

    @Override
    public OverviewStatsVO getOverviewStats() {
        OverviewStatsVO vo = new OverviewStatsVO();

        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTotalEvents(eventMapper.selectCount(null));

        List<Event> allEvents = eventMapper.selectList(null);
        Map<String, Long> statusMap = new HashMap<>();
        for (Event event : allEvents) {
            String status = event.getEventStatus() != null ? event.getEventStatus().name() : "UNKNOWN";
            statusMap.put(status, statusMap.getOrDefault(status, 0L) + 1);
        }
        vo.setEventsByStatus(statusMap);

        vo.setTotalRegistrations(registrationMapper.selectCount(null));

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        vo.setNewUsersThisMonth((long) userMapper.getUserNumsByMonth(currentMonth));

        return vo;
    }

    @Override
    public List<EventStatsVO> getEventStats() {
        // 1. 一次性查询所有赛事（按创建时间倒序）
        List<Event> allEvents = eventMapper.selectList(new LambdaQueryWrapper<Event>().orderByDesc(Event::getCreateTime));
        
        // 2. 批量聚合查询报名统计 (按 event_id 分组)
        List<Map<String, Object>> regStats = registrationMapper.countRegistrationsGroupByEvent();
        Map<Long, Map<String, Object>> regStatsMap = regStats.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("eventId")).longValue(),
                        map -> map,
                        (existing, replacement) -> existing
                ));

        // 3. 批量聚合查询成绩发布统计 (按 event_id 分组)
        List<Map<String, Object>> scoreStats = scoreMapper.countPublishedProjectsGroupByEvent();
        Map<Long, Long> scoreStatsMap = scoreStats.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("eventId")).longValue(),
                        map -> ((Number) map.get("publishedCount")).longValue(),
                        (existing, replacement) -> existing
                ));

        // 4. 组装结果
        List<EventStatsVO> result = new ArrayList<>();
        for (Event event : allEvents) {
            EventStatsVO vo = new EventStatsVO();
            vo.setEventId(event.getId());
            vo.setEventName(event.getEventName());

            Map<String, Object> eventRegStats = regStatsMap.get(event.getId());
            if (eventRegStats != null) {
                vo.setTotalRegistrations(((Number) eventRegStats.get("total")).longValue());
                Object approvedObj = eventRegStats.get("approved");
                vo.setApprovedRegistrations(approvedObj != null ? ((Number) approvedObj).longValue() : 0L);
            } else {
                vo.setTotalRegistrations(0L);
                vo.setApprovedRegistrations(0L);
            }

            vo.setPublishedProjects(scoreStatsMap.getOrDefault(event.getId(), 0L));

            result.add(vo);
        }

        return result;
    }
}

