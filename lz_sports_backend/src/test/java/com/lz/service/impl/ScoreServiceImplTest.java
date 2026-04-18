package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lz.common.exception.BusinessException;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.entity.Score;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ScoreMapper scoreMapper;

    @Spy
    @InjectMocks
    private ScoreServiceImpl scoreService;

    @Test
    void publishScoresShouldThrowWhenNoUnpublishedScores() {
        doReturn(List.of()).when(scoreService).list(org.mockito.ArgumentMatchers.<Wrapper<Score>>any());

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.publishScores(100L));
        assertEquals("没有可发布的成绩", exception.getMessage());
    }

    @Test
    void upsertScoreShouldThrowWhenRegistrationNotFound() {
        ScoreUpsertDTO dto = new ScoreUpsertDTO();
        dto.setRegistrationId(999L);
        dto.setScoreValue("12.30");
        dto.setScoreRank(1);

        when(registrationMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.upsertScore(dto));
        assertEquals("报名记录不存在", exception.getMessage());
    }

    @Test
    void updateScoreShouldThrowWhenScoreNotFound() {
        ScoreUpsertDTO dto = new ScoreUpsertDTO();
        dto.setScoreValue("11.11");
        dto.setScoreRank(2);

        doReturn(null).when(scoreService).getById(123L);

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.updateScore(123L, dto));
        assertEquals("成绩不存在", exception.getMessage());
    }

    @Test
    void updateScoreShouldThrowWhenScorePublished() {
        ScoreUpsertDTO dto = new ScoreUpsertDTO();
        dto.setScoreValue("10.50");
        dto.setScoreRank(1);

        Score score = new Score();
        score.setId(321L);
        score.setIsPublished(true);
        doReturn(score).when(scoreService).getById(321L);

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.updateScore(321L, dto));
        assertEquals("已发布的成绩不可修改", exception.getMessage());
    }
}
