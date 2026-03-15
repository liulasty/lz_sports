package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.enums.NotificationType;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.dto.RegistrationDTO;
import com.lz.entity.Athlete;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import com.lz.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;

/**
 * 报名服务实现
 */
import com.lz.common.enums.EventStatus;
import com.alibaba.excel.EasyExcel;
import com.lz.vo.RegistrationExportVO;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationMapper registrationMapper;
    private final AthleteMapper athleteMapper; // Note: athleteMapper now maps to sys_user, need verification
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient; // Requires Redisson dependency

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long projectId) {
        // Use Distributed Lock to prevent overselling
        String lockKey = "lock:registration:project:" + projectId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock for 5 seconds, hold for 10 seconds
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    Long userId = BaseContext.getCurrentId();
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        throw new BusinessException("用户不存在");
                    }
                    if (user.getStatus() == UserStatus.DISABLED) {
                        throw new BusinessException("账号已被禁用");
                    }
                    if (user.getUserType() == UserRole.SUPER_ADMIN || user.getUserType() == UserRole.EVENT_ADMIN) {
                        throw new BusinessException("管理员角色不可申请", 403);
                    }
                    
                    Athlete athlete = athleteMapper.selectByUserId(userId);
                    if (athlete == null) {
                        throw new BusinessException("请先完善运动员信息");
                    }
                    if (athlete.getAthleteState() == null || !athlete.getAthleteState().name().equals("SUCCESS")) {
                        throw new BusinessException("请先申请并通过运动员资格审核");
                    }
            
                    Project project = projectMapper.selectById(projectId);
                    if (project == null) {
                        throw new BusinessException("项目不存在");
                    }
            
                    Event event = eventMapper.selectById(project.getEventId());
                    if (event == null) {
                        throw new BusinessException("赛事不存在");
                    }
            
                    // 1. Check Event Status
                    if (event.getEventStatus() != EventStatus.OPEN) {
                        throw new BusinessException("赛事状态不是 OPEN");
                    }
            
                    Date now = new Date();
                    if (event.getRegistrationStartTime() != null && now.before(event.getRegistrationStartTime())) {
                        throw new BusinessException("不在报名时间范围内");
                    }
                    if (event.getRegistrationEndTime() != null && now.after(event.getRegistrationEndTime())) {
                        throw new BusinessException("不在报名时间范围内");
                    }
            
                    LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(Registration::getAthleteId, userId);
                    lqw.eq(Registration::getItemId, projectId);
                    lqw.ne(Registration::getRegistrationStatus, RegistrationStatus.CANCELLED);
                    if (count(lqw) > 0) {
                        throw new BusinessException("您已报名该项目，请勿重复报名");
                    }

                    int registeredCount = registrationMapper.countActiveByUserAndEvent(userId, event.getId());
                    int maxItemsPerAthlete = event.getMaxItemsPerAthlete() == null ? 1 : event.getMaxItemsPerAthlete();
                    if (registeredCount >= maxItemsPerAthlete) {
                        throw new BusinessException("已达到本赛事最多报名" + maxItemsPerAthlete + "个项目的限制");
                    }

                    if (project.getStartTime() != null && project.getEndTime() != null) {
                        String conflictItemName = registrationMapper.findConflictItemName(userId, event.getId(), project.getStartTime(), project.getEndTime());
                        if (conflictItemName != null && !conflictItemName.isEmpty()) {
                            throw new BusinessException("与您已报名的[" + conflictItemName + "]时间冲突");
                        }
                    }
            
                    if (project.getLimitation() != null && project.getLimitation() != com.lz.common.enums.GenderLimit.ALL) {
                        boolean maleProject = project.getLimitation() == com.lz.common.enums.GenderLimit.MALE;
                        boolean femaleProject = project.getLimitation() == com.lz.common.enums.GenderLimit.FEMALE;
                        if ((maleProject && !"男".equals(athlete.getGender())) || (femaleProject && !"女".equals(athlete.getGender()))) {
                            throw new BusinessException("性别不符合项目要求");
                        }
                    }
            
                    if (project.getAttendance() >= project.getMaxAttendance()) {
                        throw new BusinessException("该项目报名人数已满");
                    }
            
                    Registration registration = new Registration();
                    registration.setAthleteId(userId);
                    registration.setEventId(event.getId());
                    registration.setItemId(projectId);
                    registration.setRegistrationTime(now);
                    registration.setRegistrationStatus(RegistrationStatus.PENDING);
                    registration.setSchoolId(event.getSchoolId());
                    
                    save(registration);
            
                    int updated = projectMapper.incrementAttendance(projectId, project.getMaxAttendance());
                    if (updated == 0) {
                        throw new BusinessException("该项目报名人数已满");
                    }
                    syncAthleteProfileToUser(athlete, user);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BusinessException("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统中断");
        }
    }

    @Override
    public PageResult list(int currentPage, int pageSize, String name, String status, Date date) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, null);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult listByAthlete(int currentPage, int pageSize, String name, String status, Date date, Long athleteId) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, athleteId);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public RegistrationAndAthleteDTO getDetail(Long id) {
        Registration r = getById(id);
        if (r == null) return null;

        RegistrationAndAthleteDTO dto = new RegistrationAndAthleteDTO();
        dto.setId(r.getId());
        dto.setApplyTime(r.getRegistrationTime());
        if (r.getRegistrationStatus() != null) {
            dto.setStatus(r.getRegistrationStatus().getStatus());
        }

        Athlete athlete = athleteMapper.selectById(r.getAthleteId());
        if (athlete != null) {
            dto.setName(athlete.getName());

            dto.setAge(Integer.parseInt(athlete.getAge()));
            dto.setGender(athlete.getGender());
            dto.setContact(athlete.getContact());
            dto.setAthleteGrade(athlete.getGrade());
        }

        Event event = eventMapper.selectById(r.getEventId());
        if (event != null) {
            dto.setEventId(event.getId());
            dto.setEventName(event.getEventName());
        }

        Project project = projectMapper.selectById(r.getItemId());
        if (project != null) {
            dto.setItemId(project.getId());
            dto.setItemName(project.getItemName());
            dto.setNum(project.getAttendance());
            dto.setMaxNum(project.getMaxAttendance());
            if (project.getLimitation() != null) {
                dto.setLimitation(project.getLimitation().getLimit());
            }
            dto.setGrade(project.getGrade());
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("报名记录不存在");
        if (r.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException("已审核的申请不可重复审核");
        }
        r.setRegistrationStatus(RegistrationStatus.APPROVED);
        updateById(r);
        notificationService.create(r.getAthleteId(), "报名审核通过", "您的报名已审核通过", NotificationType.SYSTEM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("报名记录不存在");
        if (r.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException("已审核的申请不可重复审核");
        }
        r.setRegistrationStatus(RegistrationStatus.REJECTED);
        updateById(r);
        notificationService.create(r.getAthleteId(), "报名审核拒绝", "您的报名已被拒绝", NotificationType.SYSTEM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        cancel(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Registration r = getById(id);
        if (r == null) {
            throw new BusinessException("报名记录不存在");
        }
        Long currentUserId = BaseContext.getCurrentId();
        if (!r.getAthleteId().equals(currentUserId)) {
            throw new BusinessException("只能取消自己的报名", 403);
        }
        Event event = eventMapper.selectById(r.getEventId());
        if (event != null && event.getRegistrationEndTime() != null && new Date().after(event.getRegistrationEndTime())) {
            throw new BusinessException("报名截止后不可取消");
        }
        if (r.getRegistrationStatus() == RegistrationStatus.CANCELLED) {
            return;
        }
        r.setRegistrationStatus(RegistrationStatus.CANCELLED);
        updateById(r);
        int updated = projectMapper.decrementAttendance(r.getItemId());
        if (updated == 0) {
            throw new BusinessException("取消失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchAudit(List<Long> ids, boolean approve) {
        if (ids == null || ids.isEmpty()) {
            return "未提供审核ID";
        }
        int success = 0;
        int skipped = 0;
        for (Long id : ids) {
            Registration registration = getById(id);
            if (registration == null || registration.getRegistrationStatus() != RegistrationStatus.PENDING) {
                skipped++;
                continue;
            }
            if (approve) {
                registration.setRegistrationStatus(RegistrationStatus.APPROVED);
                notificationService.create(registration.getAthleteId(), "报名审核通过", "您的报名已审核通过", NotificationType.SYSTEM);
            } else {
                registration.setRegistrationStatus(RegistrationStatus.REJECTED);
                notificationService.create(registration.getAthleteId(), "报名审核拒绝", "您的报名已被拒绝", NotificationType.SYSTEM);
            }
            updateById(registration);
            success++;
        }
        return "已处理" + success + "条，跳过" + skipped + "条";
    }


    @Override
    public int getCountByAthlete(Long athleteId) {
        LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Registration::getAthleteId, athleteId);
        return (int) count(lqw);
    }

    @Override
    public void export(Long eventId, jakarta.servlet.http.HttpServletResponse response) {
        // Fetch data
        List<RegistrationDTO> list = registrationMapper.selectRegistrationList(eventId);
        
        List<RegistrationExportVO> exportList = new ArrayList<>();
        for (RegistrationDTO dto : list) {
            RegistrationExportVO vo = new RegistrationExportVO();
            vo.setRegistrationId(dto.getRegistrationId());
            vo.setEventName(dto.getEventName());
            vo.setItemName(dto.getItemName());
            vo.setAthleteName(dto.getAthleteName());
            vo.setGender(dto.getGender());
            vo.setGrade(dto.getGrade());
            vo.setContact(dto.getContact());
            vo.setRegistrationTime(dto.getRegistrationTime());
            vo.setStatus(dto.getRegistrationStatus());
            exportList.add(vo);
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("报名名单", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), RegistrationExportVO.class).sheet("名单").doWrite(exportList);
        } catch (Exception e) {
            log.error("Export failed", e);
            throw new BusinessException("导出失败");
        }
    }

    private void syncAthleteProfileToUser(Athlete athlete, User user) {
        boolean changed = false;
        if (athlete.getName() != null && !athlete.getName().isEmpty() && !athlete.getName().equals(user.getName())) {
            user.setName(athlete.getName());
            changed = true;
        }
        if (athlete.getGrade() != null && !athlete.getGrade().isEmpty()) {
            user.setClassName(athlete.getGrade());
            changed = true;
        }
        if (athlete.getContact() != null && !athlete.getContact().isEmpty() && !athlete.getContact().equals(user.getStudentId())) {
            user.setStudentId(athlete.getContact());
            changed = true;
        }
        if (changed) {
            userMapper.updateById(user);
        }
    }
}
