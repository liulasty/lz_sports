package com.lz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.entity.Registration;
import com.lz.entity.Score;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.User;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
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

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.importScores(file, 1L, "BEST_EFFORT"));
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

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.importScores(file, 1L, "BEST_EFFORT"));
        assertEquals("导入文件中没有可用数据", exception.getMessage());
    }

    @Test
    void importScoresShouldCountDuplicateRegistrationAsFailure() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ScoreImportVO first = new ScoreImportVO();
        first.setEventId(1L);
        first.setRegistrationId(1001L);
        first.setEventName("春季运动会");
        first.setItemId(2001L);
        first.setItemName("100米");
        first.setAthleteName("张三");
        first.setScoreValue("12.34");
        first.setScoreRank(1);
        first.setRemark("first");
        ScoreImportVO duplicate = new ScoreImportVO();
        duplicate.setEventId(1L);
        duplicate.setRegistrationId(1001L);
        duplicate.setEventName("春季运动会");
        duplicate.setItemId(2001L);
        duplicate.setItemName("100米");
        duplicate.setAthleteName("张三");
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
        registration.setItemId(2001L);
        registration.setAthleteId(3001L);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        when(registrationMapper.selectById(1001L)).thenReturn(registration);
        Event event = new Event();
        event.setId(1L);
        event.setEventName("春季运动会");
        when(eventMapper.selectById(1L)).thenReturn(event);
        Project project = new Project();
        project.setId(2001L);
        project.setItemName("100米");
        when(projectMapper.selectById(2001L)).thenReturn(project);
        User user = new User();
        user.setId(3001L);
        user.setName("张三");
        when(userMapper.selectById(3001L)).thenReturn(user);
        doReturn(null).when(scoreService).getOne(any());
        doNothing().when(scoreService).upsertScore(any(ScoreUpsertDTO.class));

        var result = scoreService.importScores(file, 1L, "BEST_EFFORT");
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getFailures().size());
    }

    @Test
    void importScoresShouldValidateAllRowsInStrictModeBeforeWriting() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ScoreImportVO first = new ScoreImportVO();
        first.setEventId(1L);
        first.setRegistrationId(2001L);
        first.setEventName("春季运动会");
        first.setItemId(2002L);
        first.setItemName("跳远");
        first.setAthleteName("李四");
        first.setScoreValue("11.11");
        first.setScoreRank(1);
        ScoreImportVO duplicate = new ScoreImportVO();
        duplicate.setEventId(1L);
        duplicate.setRegistrationId(2001L);
        duplicate.setEventName("春季运动会");
        duplicate.setItemId(2002L);
        duplicate.setItemName("跳远");
        duplicate.setAthleteName("李四");
        duplicate.setScoreValue("11.12");
        duplicate.setScoreRank(2);
        EasyExcel.write(out, ScoreImportVO.class).sheet("Sheet1").doWrite(List.of(first, duplicate));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "strict-dup.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        Registration registration = new Registration();
        registration.setId(2001L);
        registration.setEventId(1L);
        registration.setItemId(2002L);
        registration.setAthleteId(3002L);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        when(registrationMapper.selectById(2001L)).thenReturn(registration);
        Event event = new Event();
        event.setId(1L);
        event.setEventName("春季运动会");
        when(eventMapper.selectById(1L)).thenReturn(event);
        Project project = new Project();
        project.setId(2002L);
        project.setItemName("跳远");
        when(projectMapper.selectById(2002L)).thenReturn(project);
        User user = new User();
        user.setId(3002L);
        user.setName("李四");
        when(userMapper.selectById(3002L)).thenReturn(user);
        doReturn(null).when(scoreService).getOne(any());

        var result = scoreService.importScores(file, 1L, "STRICT");
        assertEquals("STRICT", result.getMode());
        assertEquals(true, result.getAllOrNothing());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getFailures().size());
    }

    @Test
    void importScoresShouldRejectWhenRosterFieldsMismatch() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ScoreImportVO row = new ScoreImportVO();
        row.setEventId(1L);
        row.setRegistrationId(3001L);
        row.setEventName("错误赛事名");
        row.setItemId(501L);
        row.setItemName("100米");
        row.setAthleteName("张三");
        row.setScoreValue("12.34");
        row.setScoreRank(1);
        EasyExcel.write(out, ScoreImportVO.class).sheet("Sheet1").doWrite(List.of(row));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mismatch.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        Registration registration = new Registration();
        registration.setId(3001L);
        registration.setEventId(1L);
        registration.setItemId(501L);
        registration.setAthleteId(7001L);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        when(registrationMapper.selectById(3001L)).thenReturn(registration);

        Event event = new Event();
        event.setId(1L);
        event.setEventName("正确赛事名");
        when(eventMapper.selectById(1L)).thenReturn(event);

        var result = scoreService.importScores(file, 1L, "BEST_EFFORT");
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("赛事名称与报名记录不一致", result.getFailures().get(0).getReason());
    }
}
