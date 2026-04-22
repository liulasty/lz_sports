package com.lz.service.impl;

import com.lz.common.context.BaseContext;
import com.lz.common.enums.AthleteStatus;
import com.lz.common.enums.EventStatus;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.entity.Athlete;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.EventAdminMappingMapper;
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
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private EventAdminMappingMapper eventAdminMappingMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RBucket<Object> idempotencyBucket;

    @Mock
    private RLock registrationLock;

    @Spy
    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void batchAuditShouldReturnHintWhenIdsEmpty() {
        assertEquals("未提供审核ID", registrationService.batchAudit(List.of(), true));
    }

    @Test
    void batchAuditShouldCountSuccessAndSkip() {
        // Simulate an authenticated SCHOOL_ADMIN to bypass event mapping checks
        BaseContext.setCurrentId(999L);
        User admin = new User();
        admin.setId(999L);
        admin.setUserType(UserRole.SCHOOL_ADMIN);
        when(userMapper.selectById(999L)).thenReturn(admin);

        Registration pending = new Registration();
        pending.setId(1L);
        pending.setAthleteId(100L);
        pending.setEventId(1L);
        pending.setRegistrationStatus(RegistrationStatus.PENDING);

        Registration approved = new Registration();
        approved.setId(2L);
        approved.setAthleteId(101L);
        approved.setEventId(1L);
        approved.setRegistrationStatus(RegistrationStatus.APPROVED);

        doReturn(pending).when(registrationService).getById(1L);
        doReturn(approved).when(registrationService).getById(2L);
        doReturn(true).when(registrationService).updateById(any(Registration.class));

        String result = registrationService.batchAudit(List.of(1L, 2L), true);
        assertEquals("已处理1条，跳过1条", result);

        BaseContext.removeCurrentId();
    }

    @Test
    void approveShouldRejectWhenEventAdminNotMappedToEvent() {
        Long eventAdminId = 666L;
        BaseContext.setCurrentId(eventAdminId);
        User admin = new User();
        admin.setId(eventAdminId);
        admin.setUserType(UserRole.EVENT_ADMIN);
        when(userMapper.selectById(eventAdminId)).thenReturn(admin);
        when(eventAdminMappingMapper.selectCount(any())).thenReturn(0L);

        Registration pending = new Registration();
        pending.setId(1L);
        pending.setAthleteId(100L);
        pending.setEventId(2L);
        pending.setRegistrationStatus(RegistrationStatus.PENDING);
        doReturn(pending).when(registrationService).getById(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.approve(1L));
        assertEquals("您不是该赛事的管理员", ex.getMessage());
        assertEquals(403, ex.getCode());

        BaseContext.removeCurrentId();
    }

    @Test
    void batchAuditShouldRejectWhenEventAdminNotMappedToRegistrationEvent() {
        Long eventAdminId = 666L;
        BaseContext.setCurrentId(eventAdminId);
        User admin = new User();
        admin.setId(eventAdminId);
        admin.setUserType(UserRole.EVENT_ADMIN);
        when(userMapper.selectById(eventAdminId)).thenReturn(admin);
        when(eventAdminMappingMapper.selectCount(any())).thenReturn(0L);

        Registration pending = new Registration();
        pending.setId(1L);
        pending.setAthleteId(100L);
        pending.setEventId(2L);
        pending.setRegistrationStatus(RegistrationStatus.PENDING);
        doReturn(pending).when(registrationService).getById(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.batchAudit(List.of(1L), true));
        assertEquals("您不是该赛事的管理员", ex.getMessage());
        assertEquals(403, ex.getCode());

        BaseContext.removeCurrentId();
    }

    @Test
    void batchAuditShouldRejectWhenPendingRegistrationsBelongToDifferentEvents() {
        BaseContext.setCurrentId(999L);

        Registration pending1 = new Registration();
        pending1.setId(1L);
        pending1.setAthleteId(100L);
        pending1.setEventId(1L);
        pending1.setRegistrationStatus(RegistrationStatus.PENDING);

        Registration pending2 = new Registration();
        pending2.setId(2L);
        pending2.setAthleteId(101L);
        pending2.setEventId(2L);
        pending2.setRegistrationStatus(RegistrationStatus.PENDING);

        doReturn(pending1).when(registrationService).getById(1L);
        doReturn(pending2).when(registrationService).getById(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.batchAudit(List.of(1L, 2L), true));
        assertEquals("批量审核仅支持同一赛事", ex.getMessage());
        assertEquals(400, ex.getCode());

        BaseContext.removeCurrentId();
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

    @Test
    void addShouldReactivateCancelledRegistrationInsteadOfInsert() throws InterruptedException {
        Long userId = 8L;
        Long projectId = 1L;
        Long eventId = 1L;
        BaseContext.setCurrentId(userId);

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setUserType(UserRole.ATHLETE);
        user.setName("Smoke Runner");

        Event event = new Event();
        event.setId(eventId);
        event.setEventStatus(EventStatus.OPEN);
        event.setSchoolId(1L);
        event.setRegistrationStartTime(new Date(System.currentTimeMillis() - 60000));
        event.setRegistrationEndTime(new Date(System.currentTimeMillis() + 60000));
        event.setMaxItemsPerAthlete(3);

        Project project = new Project();
        project.setId(projectId);
        project.setEventId(eventId);
        project.setMaxAttendance(30);

        Athlete athlete = new Athlete();
        athlete.setUserId(userId);
        athlete.setEventId(eventId);
        athlete.setAthleteState(AthleteStatus.APPROVED);

        Registration cancelled = new Registration();
        cancelled.setId(1L);
        cancelled.setAthleteId(userId);
        cancelled.setEventId(eventId);
        cancelled.setItemId(projectId);
        cancelled.setRegistrationStatus(RegistrationStatus.CANCELLED);

        when(redissonClient.getBucket(anyString())).thenReturn(idempotencyBucket);
        when(idempotencyBucket.trySet(eq("1"), eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(redissonClient.getLock(anyString())).thenReturn(registrationLock);
        when(registrationLock.tryLock(eq(5L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(true);

        when(userMapper.selectById(userId)).thenReturn(user);
        when(projectMapper.selectById(projectId)).thenReturn(project);
        when(eventMapper.selectById(eventId)).thenReturn(event);
        when(athleteMapper.selectOne(any())).thenReturn(athlete);
        doReturn(cancelled).when(registrationService).getOne(any());
        when(registrationMapper.countActiveByUserAndEvent(userId, eventId)).thenReturn(0);
        when(projectMapper.incrementAttendance(projectId, 30)).thenReturn(1);
        doReturn(true).when(registrationService).updateById(any(Registration.class));
        when(applicationContext.getBean(com.lz.service.RegistrationService.class)).thenReturn(registrationService);
        registrationService.setApplicationContext(applicationContext);

        registrationService.add(projectId);

        verify(registrationService).updateById(argThat(registration ->
                registration.getId().equals(1L) && registration.getRegistrationStatus() == RegistrationStatus.PENDING));
        verify(registrationService, never()).save(any(Registration.class));
        BaseContext.removeCurrentId();
    }

    @Test
    void addShouldReactivateRejectedRegistrationInsteadOfInsert() throws InterruptedException {
        Long userId = 8L;
        Long projectId = 3L;
        Long eventId = 1L;
        BaseContext.setCurrentId(userId);

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setUserType(UserRole.ATHLETE);

        Event event = new Event();
        event.setId(eventId);
        event.setEventStatus(EventStatus.OPEN);
        event.setSchoolId(1L);
        event.setRegistrationStartTime(new Date(System.currentTimeMillis() - 60000));
        event.setRegistrationEndTime(new Date(System.currentTimeMillis() + 60000));
        event.setMaxItemsPerAthlete(3);

        Project project = new Project();
        project.setId(projectId);
        project.setEventId(eventId);
        project.setMaxAttendance(20);

        Athlete athlete = new Athlete();
        athlete.setUserId(userId);
        athlete.setEventId(eventId);
        athlete.setAthleteState(AthleteStatus.APPROVED);

        Registration rejected = new Registration();
        rejected.setId(5L);
        rejected.setAthleteId(userId);
        rejected.setEventId(eventId);
        rejected.setItemId(projectId);
        rejected.setRegistrationStatus(RegistrationStatus.REJECTED);

        when(redissonClient.getBucket(anyString())).thenReturn(idempotencyBucket);
        when(idempotencyBucket.trySet(eq("1"), eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(redissonClient.getLock(anyString())).thenReturn(registrationLock);
        when(registrationLock.tryLock(eq(5L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(true);

        when(userMapper.selectById(userId)).thenReturn(user);
        when(projectMapper.selectById(projectId)).thenReturn(project);
        when(eventMapper.selectById(eventId)).thenReturn(event);
        when(athleteMapper.selectOne(any())).thenReturn(athlete);
        doReturn(rejected).when(registrationService).getOne(any());
        when(registrationMapper.countActiveByUserAndEvent(userId, eventId)).thenReturn(0);
        when(projectMapper.incrementAttendance(projectId, 20)).thenReturn(1);
        doReturn(true).when(registrationService).updateById(any(Registration.class));
        when(applicationContext.getBean(com.lz.service.RegistrationService.class)).thenReturn(registrationService);
        registrationService.setApplicationContext(applicationContext);

        registrationService.add(projectId);

        verify(registrationService).updateById(argThat(registration ->
                registration.getId().equals(5L) && registration.getRegistrationStatus() == RegistrationStatus.PENDING));
        verify(registrationService, never()).save(any(Registration.class));
        BaseContext.removeCurrentId();
    }
}
