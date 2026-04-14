package com.lz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.common.exception.BusinessException;
import com.lz.common.exception.GlobalExceptionHandler;
import com.lz.dto.SchoolInitDTO;
import com.lz.service.SchoolConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SystemInitControllerTest {

    @Mock
    private SchoolConfigService schoolConfigService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemInitController(schoolConfigService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void checkInitShouldReturnCurrentStatus() throws Exception {
        when(schoolConfigService.isInitialized()).thenReturn(true);

        mockMvc.perform(get("/api/system/init-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void initSystemShouldRejectInvalidBody() throws Exception {
        SchoolInitDTO dto = buildValidInitDTO();
        dto.setAdminUsername("");

        mockMvc.perform(post("/api/system/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("管理员用户名不能为空")));
    }

    @Test
    void initSystemShouldReturnConflictWhenAlreadyInitialized() throws Exception {
        doThrow(new BusinessException("系统已初始化，请勿重复操作", 409))
                .when(schoolConfigService).initSystem(org.mockito.ArgumentMatchers.any(SchoolInitDTO.class));

        mockMvc.perform(post("/api/system/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidInitDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.msg").value("系统已初始化，请勿重复操作"));
    }

    @Test
    void initSystemShouldDelegateToServiceWhenValid() throws Exception {
        SchoolInitDTO dto = buildValidInitDTO();

        mockMvc.perform(post("/api/system/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("系统初始化成功"));

        verify(schoolConfigService).initSystem(org.mockito.ArgumentMatchers.any(SchoolInitDTO.class));
    }

    private SchoolInitDTO buildValidInitDTO() {
        SchoolInitDTO dto = new SchoolInitDTO();
        dto.setSchoolName("LZ Sports School");
        dto.setLogoUrl("");
        dto.setThemeColor("#1677ff");
        dto.setContactEmail("contact@lz.com");
        dto.setAdminUsername("school_admin");
        dto.setAdminPassword("admin123");
        dto.setAdminEmail("admin@lz.com");
        dto.setOrgMode("UNIVERSITY");
        dto.setGrades(List.of("Engineering", "Science"));
        return dto;
    }
}
