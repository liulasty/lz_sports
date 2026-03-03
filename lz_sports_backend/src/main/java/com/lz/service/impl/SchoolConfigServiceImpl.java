package com.lz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.dto.SchoolInitDTO;
import com.lz.entity.Grade;
import com.lz.entity.SchoolConfig;
import com.lz.entity.User;
import com.lz.mapper.GradeMapper;
import com.lz.mapper.SchoolConfigMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.SchoolConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * School Config Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl extends ServiceImpl<SchoolConfigMapper, SchoolConfig> implements SchoolConfigService {

    private final UserMapper userMapper;
    private final GradeMapper gradeMapper;

    @Override
    public boolean isInitialized() {
        return this.count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSystem(SchoolInitDTO schoolInitDTO) {
        // 1. Check if already initialized
        if (isInitialized()) {
            throw new RuntimeException("系统已初始化，请勿重复操作");
        }

        // 2. Save School Config
        SchoolConfig schoolConfig = new SchoolConfig();
        schoolConfig.setSchoolName(schoolInitDTO.getSchoolName());
        schoolConfig.setLogoUrl(schoolInitDTO.getLogoUrl());
        schoolConfig.setThemeColor(schoolInitDTO.getThemeColor());
        schoolConfig.setContactEmail(schoolInitDTO.getContactEmail());
        schoolConfig.setCreateTime(LocalDateTime.now());
        schoolConfig.setUpdateTime(LocalDateTime.now());
        this.save(schoolConfig);
        Long schoolId = schoolConfig.getId();

        // 3. Create Admin User
        User adminUser = new User();
        adminUser.setUserName(schoolInitDTO.getAdminUsername());
        // Note: In a real system, password should be encrypted. Following existing pattern (plain text based on UserServiceImpl).
        adminUser.setPassword(schoolInitDTO.getAdminPassword()); 
        adminUser.setEmail(schoolInitDTO.getAdminEmail());
        adminUser.setUserType(UserRole.SCHOOL_ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRegisterTime(LocalDateTime.now());
        adminUser.setSchoolId(schoolId);
        userMapper.insert(adminUser);

        // 4. Create Grades
        List<String> grades = schoolInitDTO.getGrades();
        if (grades != null && !grades.isEmpty()) {
            for (int i = 0; i < grades.size(); i++) {
                String gradeName = grades.get(i);
                Grade grade = new Grade();
                grade.setName(gradeName);
                grade.setSchoolId(schoolId);
                grade.setSortOrder(i + 1);
                gradeMapper.insert(grade);
            }
        }
        
        log.info("System initialized successfully for school: {}", schoolInitDTO.getSchoolName());
    }
}
