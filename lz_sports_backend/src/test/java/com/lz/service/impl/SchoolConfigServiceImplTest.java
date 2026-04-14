package com.lz.service.impl;

import com.lz.common.exception.BusinessException;
import com.lz.dto.SchoolInitDTO;
import com.lz.entity.Department;
import com.lz.entity.SchoolConfig;
import com.lz.entity.User;
import com.lz.mapper.DepartmentMapper;
import com.lz.mapper.UserMapper;
import com.lz.util.ImageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolConfigServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private ImageUtils imageUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private SchoolConfigServiceImpl schoolConfigService;

    @Test
    void initSystemShouldThrowWhenAlreadyInitialized() {
        doReturn(true).when(schoolConfigService).isInitialized();

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.initSystem(buildInitDTO(null)));
        assertEquals("系统已初始化，请勿重复操作", exception.getMessage());
        assertEquals(409, exception.getCode());
    }

    @Test
    void initSystemShouldUseUniversityAsDefaultOrgMode() {
        SchoolConfig existingConfig = new SchoolConfig();
        existingConfig.setId(100L);
        doReturn(false).when(schoolConfigService).isInitialized();
        doReturn(existingConfig).when(schoolConfigService).getOne(any());
        doReturn(true).when(schoolConfigService).saveOrUpdate(any(SchoolConfig.class));
        when(imageUtils.getUrlOrDefault(any())).thenReturn("fallback-logo");
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-password");

        schoolConfigService.initSystem(buildInitDTO(null));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals(100L, userCaptor.getValue().getSchoolId());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());

        ArgumentCaptor<Department> deptCaptor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper, times(2)).insert(deptCaptor.capture());
        List<Department> departments = deptCaptor.getAllValues();
        assertEquals("UNIVERSITY", departments.get(0).getOrgMode());
        assertEquals("Engineering", departments.get(0).getCollege());
        assertEquals(null, departments.get(0).getGrade());
        assertEquals("Science", departments.get(1).getCollege());
    }

    @Test
    void initSystemShouldMapGradesForHighSchoolMode() {
        SchoolConfig existingConfig = new SchoolConfig();
        existingConfig.setId(101L);
        doReturn(false).when(schoolConfigService).isInitialized();
        doReturn(existingConfig).when(schoolConfigService).getOne(any());
        doReturn(true).when(schoolConfigService).saveOrUpdate(any(SchoolConfig.class));
        when(imageUtils.getUrlOrDefault(any())).thenReturn("fallback-logo");
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-password");

        schoolConfigService.initSystem(buildInitDTO("high_school"));

        ArgumentCaptor<Department> deptCaptor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper, times(2)).insert(deptCaptor.capture());
        List<Department> departments = deptCaptor.getAllValues();
        assertEquals("HIGH_SCHOOL", departments.get(0).getOrgMode());
        assertEquals("Engineering", departments.get(0).getGrade());
        assertTrue(departments.get(0).getCollege() == null || departments.get(0).getCollege().isEmpty());
    }

    @Test
    void initSystemShouldThrowWhenOrgModeInvalid() {
        SchoolConfig existingConfig = new SchoolConfig();
        existingConfig.setId(102L);
        doReturn(false).when(schoolConfigService).isInitialized();
        doReturn(existingConfig).when(schoolConfigService).getOne(any());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> schoolConfigService.initSystem(buildInitDTO("MIDDLE_SCHOOL")));
        assertEquals("组织模式仅支持 UNIVERSITY 或 HIGH_SCHOOL", exception.getMessage());
    }

    @Test
    void resetSystemShouldResetInitializedAndClearData() {
        SchoolConfig config = new SchoolConfig();
        config.setInitialized(true);
        doReturn(config).when(schoolConfigService).getOne(any());
        doReturn(true).when(schoolConfigService).updateById(any(SchoolConfig.class));

        schoolConfigService.resetSystem();

        ArgumentCaptor<SchoolConfig> configCaptor = ArgumentCaptor.forClass(SchoolConfig.class);
        verify(schoolConfigService).updateById(configCaptor.capture());
        assertEquals(Boolean.FALSE, configCaptor.getValue().getInitialized());
        verify(departmentMapper).delete(isNull());
        verify(userMapper).delete(any());
    }

    @Test
    void resetSystemShouldStillClearDataWhenConfigMissing() {
        doReturn(null).when(schoolConfigService).getOne(any());

        schoolConfigService.resetSystem();

        verify(schoolConfigService, never()).updateById(any(SchoolConfig.class));
        verify(departmentMapper).delete(isNull());
        verify(userMapper).delete(any());
    }

    @Test
    void initSystemShouldStopWhenAdminInsertFails() {
        SchoolConfig existingConfig = new SchoolConfig();
        existingConfig.setId(103L);
        doReturn(false).when(schoolConfigService).isInitialized();
        doReturn(existingConfig).when(schoolConfigService).getOne(any());
        doReturn(true).when(schoolConfigService).saveOrUpdate(any(SchoolConfig.class));
        when(imageUtils.getUrlOrDefault(any())).thenReturn("fallback-logo");
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-password");
        doThrow(new RuntimeException("insert fail")).when(userMapper).insert(any(User.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> schoolConfigService.initSystem(buildInitDTO("UNIVERSITY")));
        assertEquals("insert fail", exception.getMessage());
        verify(departmentMapper, never()).insert(any(Department.class));
    }

    @Test
    void uploadLogoShouldThrowWhenFileIsEmpty() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.uploadLogo(file));
        assertEquals("文件不能为空", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void uploadLogoShouldThrowWhenFileTooLarge() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(5L * 1024 * 1024 + 1);

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.uploadLogo(file));
        assertEquals("文件大小不能超过5MB", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void uploadLogoShouldThrowWhenFilenameIsMissing() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.uploadLogo(file));
        assertEquals("无效的文件", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void uploadLogoShouldThrowWhenExtensionInvalid() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("logo.bmp");

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.uploadLogo(file));
        assertEquals("仅支持 jpg/png/gif 格式", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void uploadLogoShouldReturnUrlAndUpdateConfigWhenValid() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        SchoolConfig config = new SchoolConfig();
        config.setId(200L);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("logo.png");
        doReturn(config).when(schoolConfigService).getOne(any());
        doReturn(true).when(schoolConfigService).updateById(any(SchoolConfig.class));

        String url = schoolConfigService.uploadLogo(file);

        assertEquals("https://wakobox.top/wp-content/uploads/2026/02/REN02L032_20.webp", url);
        ArgumentCaptor<SchoolConfig> configCaptor = ArgumentCaptor.forClass(SchoolConfig.class);
        verify(schoolConfigService).updateById(configCaptor.capture());
        assertEquals(url, configCaptor.getValue().getLogoUrl());
    }

    @Test
    void uploadLogoShouldThrowBusinessExceptionWhenUpdateFails() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        SchoolConfig config = new SchoolConfig();
        config.setId(201L);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("logo.jpg");
        doReturn(config).when(schoolConfigService).getOne(any());
        doThrow(new RuntimeException("db write failed")).when(schoolConfigService).updateById(any(SchoolConfig.class));

        BusinessException exception = assertThrows(BusinessException.class, () -> schoolConfigService.uploadLogo(file));
        assertEquals("上传失败", exception.getMessage());
    }

    @Test
    void uploadLogoShouldReturnPlaceholderUrlWhenConfigMissing() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("logo.jpeg");
        doReturn(null).when(schoolConfigService).getOne(any());

        String url = schoolConfigService.uploadLogo(file);

        assertTrue(url.startsWith("https://"));
        assertTrue(url.endsWith(".webp"));
        verify(schoolConfigService, never()).updateById(any(SchoolConfig.class));
        // Placeholder phase: uploadLogo should not call imageUtils upload integration yet.
        verifyNoInteractions(imageUtils);
    }

    private SchoolInitDTO buildInitDTO(String orgMode) {
        SchoolInitDTO dto = new SchoolInitDTO();
        dto.setSchoolName("LZ Sports School");
        dto.setLogoUrl("");
        dto.setThemeColor("#1677ff");
        dto.setContactEmail("contact@lz.com");
        dto.setAdminUsername("school_admin");
        dto.setAdminPassword("admin123");
        dto.setAdminEmail("admin@lz.com");
        dto.setOrgMode(orgMode);
        dto.setGrades(List.of("Engineering", "Science"));
        return dto;
    }
}
