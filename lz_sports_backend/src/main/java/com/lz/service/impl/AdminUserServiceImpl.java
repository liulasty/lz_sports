package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.UserQueryDTO;
import com.lz.entity.User;
import com.lz.mapper.UserMapper;
import com.lz.service.AdminUserService;
import com.lz.util.RedisUtil;
import com.lz.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageResult getUsers(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(User::getUserType, queryDTO.getRole());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, queryDTO.getKeyword())
                    .or()
                    .like(User::getEmail, queryDTO.getKeyword())
                    .or()
                    .like(User::getName, queryDTO.getKeyword()));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        userMapper.selectPage(page, wrapper);

        List<UserVO> voList = page.getRecords().stream()
                .map(User::toUserVO)
                .collect(Collectors.toList());

        return new PageResult((int) page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeRole(Long id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (UserRole.SUPER_ADMIN.equals(user.getUserType())) {
            throw new BusinessException("禁止修改超级管理员角色", 403);
        }
        
        UserRole targetRole;
        try {
            targetRole = UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("不支持的角色类型");
        }
        
        if (targetRole != UserRole.USER && targetRole != UserRole.EVENT_ADMIN && targetRole != UserRole.ATHLETE) {
            throw new BusinessException("仅支持设置为 USER 或 EVENT_ADMIN");
        }

        user.setUserType(targetRole);
        userMapper.updateById(user);
        
        // 角色修改后，使得旧token失效
        redisUtil.del("auth:token:" + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (UserRole.SUPER_ADMIN.equals(user.getUserType())) {
            throw new BusinessException("禁止禁用超级管理员账号", 403);
        }
        
        user.setStatus(UserStatus.DISABLED);
        userMapper.updateById(user);
        
        // 禁用后立即失效token
        redisUtil.del("auth:token:" + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatus.ACTIVE);
        userMapper.updateById(user);
    }
}
