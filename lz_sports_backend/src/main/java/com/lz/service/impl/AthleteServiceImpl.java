package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.enums.AthleteStatus;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.dto.AthleteDTO;
import com.lz.dto.AthleteUpdateDTO;
import com.lz.entity.Athlete;
import com.lz.entity.User;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.AthleteService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(AthleteDTO athleteDTO) {
        Athlete athlete = new Athlete();
        athlete.setUserId(athleteDTO.getUserId());
        athlete.setName(athleteDTO.getName());
        athlete.setAge(String.valueOf(athleteDTO.getAge()));
        athlete.setGender(athleteDTO.getGender());
        athlete.setContact(athleteDTO.getPhone());
        athlete.setGrade(athleteDTO.getGrade());
        athlete.setAthleteState(AthleteStatus.AUDITING);
        athlete.setApplyTime(LocalDateTime.now());
        
        save(athlete);
        return String.valueOf(athlete.getAthleteId());
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
    public void refusePlayer(Long userId) {
        baseMapper.refusePlayer(userId);
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
        newAthlete.setName(dto.getName() != null ? dto.getName() : athlete.getName());
        newAthlete.setAge(dto.getAge() != null ? String.valueOf(dto.getAge()) : athlete.getAge());
        newAthlete.setGender(dto.getGender() != null ? dto.getGender() : athlete.getGender());
        newAthlete.setContact(dto.getContact() != null ? dto.getContact() : athlete.getContact());
        // ... set other fields
        newAthlete.setGrade(dto.getGrade() != null ? dto.getGrade() : athlete.getGrade());
        newAthlete.setAthleteState(AthleteStatus.AUDITING);
        newAthlete.setApplyTime(LocalDateTime.now());
        
        save(newAthlete);
    }
}
