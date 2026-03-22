package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.common.result.PageResult;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.entity.Registration;

import java.util.Date;
import java.util.List;

/**
 * 报名服务接口
 */
public interface RegistrationService extends IService<Registration> {

    /**
     * 分页查询报名记录 (管理员)
     */
    PageResult list(int currentPage, int pageSize, String name, String status, Date date);

    /**
     * 分页查询报名记录 (运动员)
     */
    PageResult listByAthlete(int currentPage, int pageSize, String name, String status, Date date, Long athleteId);

    java.util.Map<String, Object> getRegistrationStatsByEvent(Long eventId);

    /**
     * 获取报名详情
     */
    RegistrationAndAthleteDTO getDetail(Long id);

    /**
     * 同意报名
     */
    void approve(Long id);

    /**
     * 拒绝报名
     */
    void refuse(Long id);

    /**
     * 删除报名记录
     */
    void delete(Long id);

    void cancel(Long id);

    String batchAudit(List<Long> ids, boolean approve);

    /**
     * 提交报名
     */
    void add(Long projectId);
    
    /**
     * 获取运动员报名总数
     */
    int getCountByAthlete(Long athleteId);

    /**
     * 导出报名名单
     */
    void export(Long eventId, jakarta.servlet.http.HttpServletResponse response);

    void syncAthleteProfileToUser(com.lz.entity.Athlete athlete, com.lz.entity.User user);
}
