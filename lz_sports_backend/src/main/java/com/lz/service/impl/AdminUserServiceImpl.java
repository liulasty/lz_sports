package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.UserQueryDTO;
import com.lz.entity.AdminUserAuditLog;
import com.lz.entity.User;
import com.lz.mapper.AdminUserAuditLogMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.AdminUserService;
import com.lz.service.SportsImgService;
import com.lz.config.AppConfig;
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
    private final SportsImgService sportsImgService;
    private final AppConfig appConfig;
    private final AdminUserAuditLogMapper adminUserAuditLogMapper;

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
                .map(user -> {
                    UserVO vo = user.toUserVO();
                    String avatarImg = sportsImgService.selectImg(user.getId(), "avatar");
                    if (avatarImg != null && !avatarImg.startsWith("http")) {
                        avatarImg = "https://" + appConfig.getBucketName() + "." + appConfig.getEndpoint() + "/" + avatarImg;
                    }
                    vo.setAvatar(avatarImg != null && !avatarImg.trim().isEmpty() ? avatarImg : com.lz.util.ImageUtils.getDefaultAvatar());
                    return vo;
                })
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

        UserRole beforeRole = user.getUserType();
        user.setUserType(targetRole);
        userMapper.updateById(user);
        saveAudit(user.getId(), "ROLE_CHANGE",
                beforeRole == null ? null : beforeRole.name(),
                targetRole.name(),
                user.getStatus() == null ? null : user.getStatus().name(),
                user.getStatus() == null ? null : user.getStatus().name(),
                "管理员修改用户角色");
        
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
        
        UserStatus beforeStatus = user.getStatus();
        user.setStatus(UserStatus.DISABLED);
        userMapper.updateById(user);
        saveAudit(user.getId(), "STATUS_CHANGE",
                user.getUserType() == null ? null : user.getUserType().name(),
                user.getUserType() == null ? null : user.getUserType().name(),
                beforeStatus == null ? null : beforeStatus.name(),
                UserStatus.DISABLED.name(),
                "管理员禁用用户");
        
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
        UserStatus beforeStatus = user.getStatus();
        user.setStatus(UserStatus.ACTIVE);
        userMapper.updateById(user);
        saveAudit(user.getId(), "STATUS_CHANGE",
                user.getUserType() == null ? null : user.getUserType().name(),
                user.getUserType() == null ? null : user.getUserType().name(),
                beforeStatus == null ? null : beforeStatus.name(),
                UserStatus.ACTIVE.name(),
                "管理员启用用户");
    }

    private void saveAudit(Long targetUserId,
                           String action,
                           String beforeRole,
                           String afterRole,
                           String beforeStatus,
                           String afterStatus,
                           String remark) {
        AdminUserAuditLog log = new AdminUserAuditLog();
        log.setOperatorId(BaseContext.getCurrentId());
        log.setTargetUserId(targetUserId);
        log.setAction(action);
        log.setBeforeRole(beforeRole);
        log.setAfterRole(afterRole);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(remark);
        log.initTime();
        adminUserAuditLogMapper.insert(log);
    }
}
