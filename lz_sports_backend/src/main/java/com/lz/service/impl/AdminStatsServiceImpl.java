package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.common.enums.EventStatus;
import com.lz.common.enums.RegistrationStatus;
import com.lz.entity.Event;
import com.lz.entity.Registration;
import com.lz.entity.Score;
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
        List<Event> allEvents = eventMapper.selectList(new LambdaQueryWrapper<Event>().orderByDesc(Event::getCreateTime));
        List<EventStatsVO> result = new ArrayList<>();

        for (Event event : allEvents) {
            EventStatsVO vo = new EventStatsVO();
            vo.setEventId(event.getId());
            vo.setEventName(event.getEventName());

            long totalReg = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, event.getId()));
            vo.setTotalRegistrations(totalReg);

            long approvedReg = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, event.getId())
                    .eq(Registration::getRegistrationStatus, RegistrationStatus.CONFIRMED));
            vo.setApprovedRegistrations(approvedReg);

            List<Score> publishedScores = scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                    .select(Score::getItemId)
                    .eq(Score::getEventId, event.getId())
                    .eq(Score::getIsPublished, true));
            long publishedProj = publishedScores.stream().map(Score::getItemId).distinct().count();
            vo.setPublishedProjects(publishedProj);

            result.add(vo);
        }

        return result;
    }
}

