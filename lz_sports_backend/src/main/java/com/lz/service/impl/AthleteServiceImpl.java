package com.lz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.enums.AthleteStatus;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.common.enums.NotificationType;
import com.lz.dto.AthleteDTO;
import com.lz.dto.AthleteUpdateDTO;
import com.lz.entity.Athlete;
import com.lz.entity.User;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.AthleteService;
import com.lz.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Athlete Service Implementation
 */
@Service
@RequiredArgsConstructor
public class AthleteServiceImpl extends ServiceImpl<AthleteMapper, Athlete> implements AthleteService {

    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    public java.util.List<Athlete> getMyApplications() {
        Long currentUserId = com.lz.common.context.BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        return baseMapper.selectList(new LambdaQueryWrapper<Athlete>()
                .eq(Athlete::getUserId, currentUserId)
                .orderByDesc(Athlete::getApplyTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(AthleteDTO athleteDTO) {
        Long userId = athleteDTO.getUserId();
        Long eventId = athleteDTO.getEventId();
        
        if (eventId == null) {
            throw new BusinessException("赛事ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getUserType() == UserRole.SUPER_ADMIN || user.getUserType() == UserRole.EVENT_ADMIN) {
            throw new BusinessException("当前角色不可申请运动员", 403);
        }
        
        Athlete exists = baseMapper.selectOne(new LambdaQueryWrapper<Athlete>()
                .eq(Athlete::getUserId, userId)
                .eq(Athlete::getEventId, eventId));
                
        if (exists != null && exists.getAthleteState() == AthleteStatus.PENDING) {
            throw new BusinessException("已有待审核申请，请勿重复提交");
        }
        Athlete athlete = new Athlete();
        athlete.setUserId(userId);
        athlete.setEventId(eventId);
        athlete.setName(athleteDTO.getName());
        athlete.setAge(String.valueOf(athleteDTO.getAge()));
        athlete.setGender(athleteDTO.getGender());
        athlete.setContact(athleteDTO.getPhone());
        athlete.setGrade(athleteDTO.getGrade());
        athlete.setAthleteState(AthleteStatus.PENDING);
        athlete.setApplyTime(LocalDateTime.now());
        
        save(athlete);
        return String.valueOf(athlete.getId());
    }

    @Override
    public Athlete selectApply(Long userId) {
        Athlete athlete = baseMapper.selectByUserId(userId);
        // If not found, returning null might be handled by controller or throw exception.
        // Old code threw NoAthleteException.
        if (athlete == null) {
            throw new BusinessException("未找到运动员申请记录");
        }
        return athlete;
    }

    @Override
    public PageResult getAthleteApplicationsByEvent(Long eventId, String status, String keyword, Integer page, Integer size) {
        Page<Athlete> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<Athlete>()
                .eq(Athlete::getEventId, eventId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Athlete::getAthleteState, AthleteStatus.valueOf(status));
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Athlete::getName, keyword);
        }
        wrapper.orderByDesc(Athlete::getApplyTime);
        
        IPage<Athlete> result = baseMapper.selectPage(pageParam, wrapper);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAthleteApplication(Long eventId, Long applicationId) {
        Athlete athlete = getById(applicationId);
        if (athlete == null || !athlete.getEventId().equals(eventId)) {
            throw new BusinessException("申请记录不存在或不属于该赛事");
        }
        if (athlete.getAthleteState() != AthleteStatus.PENDING) {
            throw new BusinessException("仅待审核状态可操作");
        }
        athlete.setAthleteState(AthleteStatus.APPROVED);
        athlete.setAgreeTime(LocalDateTime.now());
        updateById(athlete);
        
        User user = userMapper.selectById(athlete.getUserId());
        if (user != null && user.getUserType() != UserRole.ATHLETE) {
            user.setUserType(UserRole.ATHLETE);
            userMapper.updateById(user);
        }
        
        notificationService.create(athlete.getUserId(), "运动员资格审核通过", "您在赛事的运动员资格申请已通过", NotificationType.ATHLETE_APPROVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAthleteApplication(Long eventId, Long applicationId, String reason) {
        Athlete athlete = getById(applicationId);
        if (athlete == null || !athlete.getEventId().equals(eventId)) {
            throw new BusinessException("申请记录不存在或不属于该赛事");
        }
        if (athlete.getAthleteState() != AthleteStatus.PENDING) {
            throw new BusinessException("仅待审核状态可操作");
        }
        athlete.setAthleteState(AthleteStatus.REJECTED);
        updateById(athlete);
        
        notificationService.create(athlete.getUserId(), "运动员资格审核未通过", "您在赛事的运动员资格申请未通过。原因：" + (reason != null ? reason : "无"), NotificationType.ATHLETE_REJECTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApproveAthleteApplications(Long eventId, java.util.List<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) return;
        for (Long id : applicationIds) {
            approveAthleteApplication(eventId, id);
        }
    }

    @Override
    public void refusePlayer(Long userId) {
        baseMapper.refusePlayer(userId);
        notificationService.create(userId, "运动员审核结果", "您的运动员申请已被拒绝", NotificationType.ATHLETE_REJECTED);
    }

    @Override
    public void deleteByUserId(Long userId) {
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Athlete::getUserId, userId);
        remove(wrapper);
    }

    @Override
    public Athlete selectOne(Long athleteId) {
        return getById(athleteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long athleteId, AthleteUpdateDTO dto) {
        Athlete athlete = getById(athleteId);
        if (athlete == null) {
            throw new BusinessException("运动员不存在");
        }

        // Delete existing record
        removeById(athleteId);

        // Update User Type to "Student" or "Athlete"
        User user = userMapper.selectById(athlete.getUserId());
        if (user != null) {
            user.setUserType(UserRole.ATHLETE);
            userMapper.updateById(user);
        }

        // Create new application
        Athlete newAthlete = new Athlete();
        newAthlete.setUserId(athlete.getUserId());
        newAthlete.setEventId(athlete.getEventId());
        newAthlete.setName(dto.getName() != null ? dto.getName() : athlete.getName());
        newAthlete.setAge(dto.getAge() != null ? String.valueOf(dto.getAge()) : athlete.getAge());
        newAthlete.setGender(dto.getGender() != null ? dto.getGender() : athlete.getGender());
        newAthlete.setContact(dto.getContact() != null ? dto.getContact() : athlete.getContact());
        // ... set other fields
        newAthlete.setGrade(dto.getGrade() != null ? dto.getGrade() : athlete.getGrade());
        newAthlete.setAthleteState(AthleteStatus.PENDING);
        newAthlete.setApplyTime(LocalDateTime.now());
        
        save(newAthlete);
    }
}
