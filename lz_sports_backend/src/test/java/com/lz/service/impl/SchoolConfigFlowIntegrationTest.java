package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.common.enums.UserRole;
import com.lz.dto.SchoolConfigUpdateDTO;
import com.lz.dto.SchoolInitDTO;
import com.lz.entity.SchoolConfig;
import com.lz.entity.User;
import com.lz.mapper.DepartmentMapper;
import com.lz.mapper.SchoolConfigMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.SchoolConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class SchoolConfigFlowIntegrationTest {

    @Autowired
    private SchoolConfigService schoolConfigService;
    @Autowired
    private SchoolConfigMapper schoolConfigMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        schoolConfigMapper.delete(null);
        departmentMapper.delete(null);
        userMapper.delete(new LambdaQueryWrapper<User>()
                .ne(User::getUserType, UserRole.SUPER_ADMIN)
                .ne(User::getUserType, UserRole.SCHOOL_ADMIN));
    }

    @Test
    void flowShouldInitAndExposeConfig() {
        SchoolInitDTO dto = buildInitDTO("UNIVERSITY");

        schoolConfigService.initSystem(dto);

        assertTrue(schoolConfigService.isInitialized());
        SchoolConfig config = schoolConfigService.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
        assertNotNull(config);
        assertEquals(dto.getSchoolName(), config.getSchoolName());
        assertEquals("UNIVERSITY", config.getOrgMode());
    }

    @Test
    void flowShouldUpdateUploadLogoAndQuery() {
        SchoolInitDTO initDTO = buildInitDTO("HIGH_SCHOOL");
        schoolConfigService.initSystem(initDTO);

        SchoolConfigUpdateDTO updateDTO = new SchoolConfigUpdateDTO();
        updateDTO.setSchoolName("Updated School");
        updateDTO.setThemeColor("#123456");
        schoolConfigService.updateSchoolConfig(updateDTO);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("logo.png");

        String logoUrl = schoolConfigService.uploadLogo(file);

        SchoolConfig config = schoolConfigService.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
        assertNotNull(config);
        assertEquals("Updated School", config.getSchoolName());
        assertEquals("#123456", config.getThemeColor());
        assertEquals(logoUrl, config.getLogoUrl());
        assertTrue(logoUrl.startsWith("https://"));
    }

    private SchoolInitDTO buildInitDTO(String orgMode) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        SchoolInitDTO dto = new SchoolInitDTO();
        dto.setSchoolName("LZ Sports School " + suffix);
        dto.setLogoUrl("");
        dto.setThemeColor("#1677ff");
        dto.setContactEmail("contact+" + suffix + "@lz.com");
        dto.setAdminUsername("school_admin_" + suffix);
        dto.setAdminPassword("admin123");
        dto.setAdminEmail("admin+" + suffix + "@lz.com");
        dto.setOrgMode(orgMode);
        dto.setGrades(List.of("Engineering", "Science"));
        return dto;
    }
}
