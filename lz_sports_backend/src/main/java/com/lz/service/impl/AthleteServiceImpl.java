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
    private final com.lz.mapper.EventMapper eventMapper;
    private final com.lz.mapper.RegistrationMapper registrationMapper;

    @Override
    public java.util.List<Athlete> getMyApplications() {
        Long currentUserId = com.lz.common.context.BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        java.util.List<Athlete> athletes = baseMapper.selectList(new LambdaQueryWrapper<Athlete>()
                .eq(Athlete::getUserId, currentUserId)
                .orderByDesc(Athlete::getApplyTime));
        
        for (Athlete athlete : athletes) {
            com.lz.entity.Event event = eventMapper.selectById(athlete.getEventId());
            if (event != null) {
                athlete.setEventName(event.getEventName());
            }
        }
        return athletes;
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
                
        if (exists != null) {
            if (exists.getAthleteState() == AthleteStatus.PENDING) {
                throw new BusinessException("该赛事已有待审核申请，请勿重复提交");
            } else if (exists.getAthleteState() == AthleteStatus.APPROVED) {
                throw new BusinessException("您已通过该赛事的运动员资格审核");
            } else if (exists.getAthleteState() == AthleteStatus.REJECTED) {
                // 如果之前被拒绝，允许重新申请，但为了避免数据库唯一索引冲突，先删除旧记录
                removeById(exists.getId());
            }
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
    public Athlete selectApply(Long userId, Long eventId) {
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<Athlete>().eq(Athlete::getUserId, userId);
        if (eventId != null) {
            wrapper.eq(Athlete::getEventId, eventId);
        }
        wrapper.orderByDesc(Athlete::getApplyTime).last("LIMIT 1");
        
        Athlete athlete = baseMapper.selectOne(wrapper);
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
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long id) {
        Athlete athlete = getById(id);
        if (athlete == null) {
            throw new BusinessException("申请记录不存在");
        }
        
        Long userId = athlete.getUserId();
        Long eventId = athlete.getEventId();
        
        // 检查是否有报名记录
        long regCount = registrationMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lz.entity.Registration>()
                .eq(com.lz.entity.Registration::getAthleteId, userId)
                .eq(com.lz.entity.Registration::getEventId, eventId));
                
        if (regCount > 0) {
            throw new BusinessException("该申请已有关联的项目报名记录，无法删除");
        }
        
        removeById(id);
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

        // 修改申请信息，重置为待审核状态
        athlete.setName(dto.getName() != null ? dto.getName() : athlete.getName());
        athlete.setAge(dto.getAge() != null ? String.valueOf(dto.getAge()) : athlete.getAge());
        athlete.setGender(dto.getGender() != null ? dto.getGender() : athlete.getGender());
        athlete.setContact(dto.getContact() != null ? dto.getContact() : athlete.getContact());
        athlete.setGrade(dto.getGrade() != null ? dto.getGrade() : athlete.getGrade());
        athlete.setAthleteState(AthleteStatus.PENDING);
        athlete.setApplyTime(LocalDateTime.now());
        
        updateById(athlete);
    }
}
