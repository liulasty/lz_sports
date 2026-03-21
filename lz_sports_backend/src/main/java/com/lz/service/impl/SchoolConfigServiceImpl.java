package com.lz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.dto.SchoolConfigUpdateDTO;
import com.lz.dto.SchoolInitDTO;
import com.lz.entity.Grade;
import com.lz.entity.SchoolConfig;
import com.lz.entity.User;
import com.lz.mapper.GradeMapper;
import com.lz.mapper.SchoolConfigMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.SchoolConfigService;
import com.lz.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

/**
 * School Config Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl extends ServiceImpl<SchoolConfigMapper, SchoolConfig> implements SchoolConfigService {

    private final UserMapper userMapper;
    private final GradeMapper gradeMapper;
    private final ImageUtils imageUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean isInitialized() {
        // 仅当存在配置且 is_initialized 为 true 时才视为已初始化
        return this.count(new LambdaQueryWrapper<SchoolConfig>()
                .eq(SchoolConfig::isInitialized, true)) > 0;
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
        
        // Use fallback if logo URL is missing
        String logoUrl = imageUtils.getUrlOrDefault(schoolInitDTO.getLogoUrl());
        schoolConfig.setLogoUrl(logoUrl);
        
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
        adminUser.setPassword(passwordEncoder.encode(schoolInitDTO.getAdminPassword()));
        adminUser.setEmail(schoolInitDTO.getAdminEmail());
        adminUser.setUserType(UserRole.SCHOOL_ADMIN);
        adminUser.setIsFirstLogin(true);
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
