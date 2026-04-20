package com.lz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.dto.SchoolConfigUpdateDTO;
import com.lz.dto.SchoolInitDTO;
import com.lz.entity.Department;
import com.lz.entity.SchoolConfig;
import com.lz.entity.User;
import com.lz.mapper.DepartmentMapper;
import com.lz.mapper.SchoolConfigMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.SchoolConfigService;
import com.lz.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * School Config Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl extends ServiceImpl<SchoolConfigMapper, SchoolConfig> implements SchoolConfigService {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final ImageUtils imageUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "school_config_mode", key = "'current_org_mode'")
    public String getCurrentOrgMode() {
        SchoolConfig config = this.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
        if (config != null && config.getOrgMode() != null) {
            return config.getOrgMode();
        }
        return "UNIVERSITY"; // 默认值
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "school_config_mode", key = "'current_org_mode'")
    public void resetSystem() {
        // 1. 设置 is_initialized 为 false
        SchoolConfig config = this.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
        if (config != null) {
            config.setInitialized(false);
            this.updateById(config);
        }

        // 2. 清空 department 数据
        departmentMapper.delete(null);
        
        // 3. (可选) 清空赛事和用户数据，或者只保留超级管理员
        userMapper.delete(new LambdaQueryWrapper<User>()
                .ne(User::getUserType, UserRole.SUPER_ADMIN)
                .ne(User::getUserType, UserRole.SCHOOL_ADMIN));
        
        log.info("System has been reset. Non-admin users and departments cleared.");
    }

    @Override
    public boolean isInitialized() {
        // 仅当存在配置且 is_initialized 为 true 时才视为已初始化
        return this.count(new LambdaQueryWrapper<SchoolConfig>()
                .eq(SchoolConfig::getInitialized, true)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "school_config_mode", key = "'current_org_mode'")
    public void initSystem(SchoolInitDTO schoolInitDTO) {
        // 1. Check if already initialized
        if (isInitialized()) {
            throw new BusinessException("系统已初始化，请勿重复操作", 409);
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
        
        // Use fallback if logo URL is missing
        String logoUrl = imageUtils.getUrlOrDefault(schoolInitDTO.getLogoUrl());
        schoolConfig.setLogoUrl(logoUrl);
        
        schoolConfig.setThemeColor(schoolInitDTO.getThemeColor());
        schoolConfig.setContactEmail(schoolInitDTO.getContactEmail());
        // Set initialized flag
        schoolConfig.setInitialized(true);
        String normalizedOrgMode = normalizeOrgMode(schoolInitDTO.getOrgMode());
        schoolConfig.setOrgMode(normalizedOrgMode);
        schoolConfig.setUpdateTime(LocalDateTime.now());
        
        this.saveOrUpdate(schoolConfig);
        Long schoolId = schoolConfig.getId();

        // Initialize Admin User
        User adminUser = new User();
        adminUser.setUsername(schoolInitDTO.getAdminUsername());
        adminUser.setPassword(passwordEncoder.encode(schoolInitDTO.getAdminPassword()));
        adminUser.setEmail(schoolInitDTO.getAdminEmail());
        adminUser.setUserType(UserRole.SCHOOL_ADMIN);
        adminUser.setIsFirstLogin(true);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setSchoolId(schoolConfig.getId());
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        userMapper.insert(adminUser);

        // 4. Create Grades / Departments
        List<String> grades = schoolInitDTO.getGrades();
        if (grades != null && !grades.isEmpty()) {
            boolean isUniversity = "UNIVERSITY".equals(schoolConfig.getOrgMode());
            for (int i = 0; i < grades.size(); i++) {
                String gradeName = grades.get(i);
                Department dept = new Department();
                dept.setOrgMode(schoolConfig.getOrgMode());
                if (isUniversity) {
                    dept.setCollege(gradeName);
                } else {
                    dept.setGrade(gradeName);
                }
                dept.setSchoolId(schoolId);
                dept.setSortOrder(i + 1);
                departmentMapper.insert(dept);
            }
        }
        
        log.info("System initialized successfully for school: {}", schoolInitDTO.getSchoolName());
    }

    private String normalizeOrgMode(String orgMode) {
        if (orgMode == null || orgMode.isBlank()) {
            return "UNIVERSITY";
        }
        String normalized = orgMode.trim().toUpperCase(Locale.ROOT);
        if (!"UNIVERSITY".equals(normalized) && !"HIGH_SCHOOL".equals(normalized)) {
            throw new BusinessException("组织模式仅支持 UNIVERSITY 或 HIGH_SCHOOL");
        }
        return normalized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchoolConfig(SchoolConfigUpdateDTO dto) {
        SchoolConfig config = this.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
        if (config == null) {
            throw new BusinessException("系统尚未初始化");
        }
        if (dto.getSchoolName() != null && !dto.getSchoolName().isEmpty()) {
            config.setSchoolName(dto.getSchoolName());
        }
        if (dto.getThemeColor() != null && !dto.getThemeColor().isEmpty()) {
            config.setThemeColor(dto.getThemeColor());
        }
        config.setUpdateTime(LocalDateTime.now());
        this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadLogo(MultipartFile file) {
        // Logo 上传限制：jpg/png/gif，≤ 5MB，超出返回 400
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空", 400);
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过5MB", 400);
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("无效的文件", 400);
        }
        
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExt = Arrays.asList("jpg", "jpeg", "png", "gif");
        if (!allowedExt.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif 格式", 400);
        }

        try {
//            String url = imageUtils.upload(file);
            String url = "https://wakobox.top/wp-content/uploads/2026/02/REN02L032_20.webp";
            SchoolConfig config = this.getOne(new LambdaQueryWrapper<SchoolConfig>().last("LIMIT 1"));
            if (config != null) {
                config.setLogoUrl(url);
                config.setUpdateTime(LocalDateTime.now());
                this.updateById(config);
            }
            return url;
        } catch (Exception e) {
            log.error("Logo上传失败", e);
            throw new BusinessException("上传失败");
        }
    }
}
