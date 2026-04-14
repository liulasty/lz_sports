package com.lz.service.impl;

import com.lz.common.enums.EventStatus;
import com.lz.common.exception.BusinessException;
import com.lz.entity.Event;
import com.lz.mapper.EventAdminMappingMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import com.lz.service.SportsImgService;
import com.lz.util.ImageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventMapper eventMapper;
    @Mock
    private SportsImgService sportsImgService;
    @Mock
    private EventAdminMappingMapper eventAdminMappingMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private RegistrationMapper registrationMapper;
    @Mock
    private ImageUtils imageUtils;
    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void changeStatusShouldThrowWhenEventNotExists() {
        doReturn(null).when(eventService).getById(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> eventService.changeStatus(1L, "OPEN"));
        assertEquals("赛事不存在", exception.getMessage());
    }

    @Test
    void changeStatusShouldThrowWhenStatusInvalid() {
        Event event = Event.builder().eventStatus(EventStatus.DRAFT).build();
        doReturn(event).when(eventService).getById(2L);

        BusinessException exception = assertThrows(BusinessException.class, () -> eventService.changeStatus(2L, "INVALID"));
        assertEquals("无效的状态: INVALID", exception.getMessage());
    }

    @Test
    void changeStatusShouldThrowWhenOpenWithoutProjects() {
        Event event = Event.builder().eventStatus(EventStatus.DRAFT).build();
        doReturn(event).when(eventService).getById(3L);
        org.mockito.Mockito.when(projectMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> eventService.changeStatus(3L, "OPEN"));
        assertEquals("发布失败：至少需要一个赛事项目", exception.getMessage());
    }

    @Test
    void changeStatusShouldThrowWhenOpenWithoutAdmins() {
        Event event = Event.builder().eventStatus(EventStatus.DRAFT).build();
        doReturn(event).when(eventService).getById(4L);
        org.mockito.Mockito.when(projectMapper.selectCount(any())).thenReturn(1L);
        org.mockito.Mockito.when(eventAdminMappingMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> eventService.changeStatus(4L, "OPEN"));
        assertEquals("发布失败：至少需要一个赛事管理员", exception.getMessage());
    }
}
