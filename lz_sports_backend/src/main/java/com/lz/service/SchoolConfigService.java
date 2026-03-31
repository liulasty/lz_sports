package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.entity.SchoolConfig;
import com.lz.dto.SchoolInitDTO;
import com.lz.dto.SchoolConfigUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Administrator
 */
public interface SchoolConfigService extends IService<SchoolConfig> {
    /**
     * Check if system is initialized
     */
    boolean isInitialized();

    /**
     * Initialize system
     */
    void initSystem(SchoolInitDTO schoolInitDTO);

    void updateSchoolConfig(SchoolConfigUpdateDTO dto);

    String uploadLogo(MultipartFile file);

    /**
     * 获取当前的组织架构模式 (UNIVERSITY 或 K12)
     */
    String getCurrentOrgMode();
}
