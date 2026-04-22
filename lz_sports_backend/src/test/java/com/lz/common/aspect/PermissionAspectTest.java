package com.lz.common.aspect;

import com.lz.common.context.BaseContext;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.entity.EventAdminMapping;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.EventAdminMappingMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAspectTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private EventAdminMappingMapper eventAdminMappingMapper;

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private PermissionAspect permissionAspect;

    @AfterEach
    void cleanup() {
        BaseContext.removeCurrentId();
    }

    @Test
    void shouldResolveEventFromAttendRegistrationId() throws Exception {
        BaseContext.setCurrentId(10L);
        mockEventAdminUser(10L);

        Method method = DummyEndpoints.class.getMethod("attend", Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"id"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{200L});

        Registration registration = new Registration();
        registration.setId(200L);
        registration.setEventId(88L);
        when(registrationMapper.selectById(200L)).thenReturn(registration);

        EventAdminMapping mapping = new EventAdminMapping();
        mapping.setEventId(88L);
        mapping.setUserId(10L);
        when(eventAdminMappingMapper.selectOne(any())).thenReturn(mapping);

        permissionAspect.checkEventAdmin(joinPoint, null);

        verify(registrationMapper).selectById(200L);
        verify(eventAdminMappingMapper).selectOne(any());
    }

    @Test
    void shouldNotTreatArbitraryLongAsEventId() throws Exception {
        BaseContext.setCurrentId(10L);
        mockEventAdminUser(10L);

        Method method = DummyEndpoints.class.getMethod("updateScore", Long.class, Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"scoreId", "dto"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{999L, new Object()});

        BusinessException ex = assertThrows(BusinessException.class, () -> permissionAspect.checkEventAdmin(joinPoint, null));
        assertEquals("无法验证赛事权限", ex.getMessage());
        assertEquals(403, ex.getCode());

        verify(eventAdminMappingMapper, never()).selectOne(any());
    }

    @Test
    void shouldRejectBatchAuditWithCrossEventIds() throws Exception {
        BaseContext.setCurrentId(10L);
        mockEventAdminUser(10L);

        Method method = DummyEndpoints.class.getMethod("batchAudit", List.class, boolean.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"ids", "approve"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{List.of(1L, 2L), true});

        Registration first = new Registration();
        first.setId(1L);
        first.setEventId(101L);
        Registration second = new Registration();
        second.setId(2L);
        second.setEventId(202L);
        when(registrationMapper.selectById(1L)).thenReturn(first);
        when(registrationMapper.selectById(2L)).thenReturn(second);

        BusinessException ex = assertThrows(BusinessException.class, () -> permissionAspect.checkEventAdmin(joinPoint, null));
        assertEquals("批量审核包含多个赛事，无法验证权限", ex.getMessage());
        assertEquals(400, ex.getCode());
    }

    private void mockEventAdminUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUserType(UserRole.EVENT_ADMIN);
        when(userMapper.selectById(userId)).thenReturn(user);
    }

    static class DummyEndpoints {
        public void attend(Long id) {
        }

        public void batchAudit(List<Long> ids, boolean approve) {
        }

        public void updateScore(Long scoreId, Object dto) {
        }
    }
}
