package com.lz.service.impl;

import com.lz.common.context.BaseContext;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.entity.Registration;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private AthleteMapper athleteMapper;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedissonClient redissonClient;

    @Spy
    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void batchAuditShouldReturnHintWhenIdsEmpty() {
        assertEquals("未提供审核ID", registrationService.batchAudit(List.of(), true));
    }

    @Test
    void batchAuditShouldCountSuccessAndSkip() {
        Registration pending = new Registration();
        pending.setId(1L);
        pending.setAthleteId(100L);
        pending.setRegistrationStatus(RegistrationStatus.PENDING);

        Registration approved = new Registration();
        approved.setId(2L);
        approved.setAthleteId(101L);
        approved.setRegistrationStatus(RegistrationStatus.APPROVED);

        doReturn(pending).when(registrationService).getById(1L);
        doReturn(approved).when(registrationService).getById(2L);
        doReturn(true).when(registrationService).updateById(any(Registration.class));

        String result = registrationService.batchAudit(List.of(1L, 2L), true);

        assertEquals("已处理1条，跳过1条", result);
    }

    @Test
    void cancelShouldRejectOtherUsersRegistration() {
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setAthleteId(100L);
        registration.setRegistrationStatus(RegistrationStatus.PENDING);
        doReturn(registration).when(registrationService).getById(1L);
        BaseContext.setCurrentId(200L);

        BusinessException exception = assertThrows(BusinessException.class, () -> registrationService.cancel(1L));
        assertEquals("只能取消自己的报名", exception.getMessage());
        assertEquals(403, exception.getCode());

        BaseContext.removeCurrentId();
    }

    @Test
    void cancelShouldThrowWhenRegistrationNotFound() {
        doReturn(null).when(registrationService).getById(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> registrationService.cancel(999L));
        assertEquals("报名记录不存在", exception.getMessage());
    }
}
