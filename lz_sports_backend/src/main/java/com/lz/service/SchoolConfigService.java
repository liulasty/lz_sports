package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.entity.SchoolConfig;
import com.lz.dto.SchoolInitDTO;

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
}
