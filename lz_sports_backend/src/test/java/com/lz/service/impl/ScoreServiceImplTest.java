package com.lz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.entity.Registration;
import com.lz.entity.Score;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import com.lz.vo.ScoreImportVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

    @Test
    void importScoresShouldRejectNonExcelFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.txt",
                "text/plain",
                "not-excel".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.importScores(file, 1L));
        assertEquals("仅支持导入 xls/xlsx 文件", exception.getMessage());
    }

    @Test
    void importScoresShouldRejectEmptyExcelRows() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, ScoreImportVO.class).sheet("Sheet1").doWrite(List.of());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.importScores(file, 1L));
        assertEquals("导入文件中没有可用数据", exception.getMessage());
    }

    @Test
    void importScoresShouldCountDuplicateRegistrationAsFailure() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ScoreImportVO first = new ScoreImportVO();
        first.setRegistrationId(1001L);
        first.setScoreValue("12.34");
        first.setScoreRank(1);
        first.setRemark("first");
        ScoreImportVO duplicate = new ScoreImportVO();
        duplicate.setRegistrationId(1001L);
        duplicate.setScoreValue("12.35");
        duplicate.setScoreRank(2);
        duplicate.setRemark("duplicate");
        EasyExcel.write(out, ScoreImportVO.class).sheet("Sheet1").doWrite(List.of(first, duplicate));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dup.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        Registration registration = new Registration();
        registration.setId(1001L);
        registration.setEventId(1L);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        when(registrationMapper.selectById(1001L)).thenReturn(registration);
        doReturn(null).when(scoreService).getOne(any());
        doNothing().when(scoreService).upsertScore(any(ScoreUpsertDTO.class));

        var result = scoreService.importScores(file, 1L);
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getFailures().size());
    }
}
