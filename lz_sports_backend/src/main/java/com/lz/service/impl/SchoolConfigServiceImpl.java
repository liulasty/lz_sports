package com.lz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        // 仅当存在配置且 is_initialized 为 true 时才视为已初始化
        return this.count(new LambdaQueryWrapper<SchoolConfig>()
                .eq(SchoolConfig::isInitialized, true)) > 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSystem(SchoolInitDTO schoolInitDTO) {
        // 1. Check if already initialized
        if (isInitialized()) {
            throw new RuntimeException("系统已初始化，请勿重复操作");
        }

        // 2. Save or Update School Config
        SchoolConfig schoolConfig;
        SchoolConfig existingConfig = this.getOne(new LambdaQueryWrapper<SchoolConfig>()
                .last("LIMIT 1"));
        
        if (existingConfig != null) {
            schoolConfig = existingConfig;
        } else {
            schoolConfig = new SchoolConfig();
            schoolConfig.setCreateTime(LocalDateTime.now());
        }
        
        schoolConfig.setSchoolName(schoolInitDTO.getSchoolName());
        schoolConfig.setLogoUrl(schoolInitDTO.getLogoUrl());
        schoolConfig.setThemeColor(schoolInitDTO.getThemeColor());
        schoolConfig.setContactEmail(schoolInitDTO.getContactEmail());
        // Set initialized flag
        schoolConfig.setInitialized(true);
        schoolConfig.setUpdateTime(LocalDateTime.now());
        
        this.saveOrUpdate(schoolConfig);
        Long schoolId = schoolConfig.getId();

        // Initialize Admin User
        User adminUser = new User();
        adminUser.setUsername(schoolInitDTO.getAdminUsername());
        adminUser.setPassword(schoolInitDTO.getAdminPassword()); // Should be encrypted
        adminUser.setEmail(schoolInitDTO.getAdminEmail());
        adminUser.setUserType(UserRole.SUPER_ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setSchoolId(schoolConfig.getId());
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
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
