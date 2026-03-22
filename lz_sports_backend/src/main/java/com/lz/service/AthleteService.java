package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.dto.AthleteDTO;
import com.lz.dto.AthleteUpdateDTO;
import com.lz.entity.Athlete;

/**
 * Athlete Service Interface
 */
public interface AthleteService extends IService<Athlete> {
    
    /**
     * Submit athlete application
     */
    String add(AthleteDTO athleteDTO);

    /**
     * Get application by User ID
     */
    Athlete selectApply(Long userId);

    /**
     * Refuse application (by User ID)
     */
    void refusePlayer(Long userId);

    /**
     * Delete by User ID
     */
    void deleteByUserId(Long userId);

    /**
     * Get Athlete by Athlete ID
     */
    Athlete selectOne(Long athleteId);

    /**
     * Get all applications for current user
     */
    java.util.List<Athlete> getMyApplications();

    /**
     * Get applications by event ID
     */
    com.lz.common.result.PageResult getAthleteApplicationsByEvent(Long eventId, String status, String keyword, Integer page, Integer size);

    void approveAthleteApplication(Long eventId, Long applicationId);

    void rejectAthleteApplication(Long eventId, Long applicationId, String reason);

    void batchApproveAthleteApplications(Long eventId, java.util.List<Long> applicationIds);

    /**
     * Update athlete (Re-apply process)
     */
    void update(Long athleteId, AthleteUpdateDTO athleteUpdateDTO);
}
