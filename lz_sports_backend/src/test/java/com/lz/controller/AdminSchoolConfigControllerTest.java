package com.lz.controller;

import com.lz.common.exception.GlobalExceptionHandler;
import com.lz.service.SchoolConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSchoolConfigControllerTest {

    @Mock
    private SchoolConfigService schoolConfigService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminSchoolConfigController(schoolConfigService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resetSystemShouldDelegateToService() throws Exception {
        mockMvc.perform(post("/api/admin/school-config/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(schoolConfigService).resetSystem();
    }
}
