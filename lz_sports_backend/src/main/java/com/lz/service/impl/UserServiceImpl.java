package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.AthleteStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventListDTO;
import com.lz.dto.UserLoginDTO;
import com.lz.dto.UserRegisterDTO;
import com.lz.entity.User;
import com.lz.entity.Athlete;
import com.lz.entity.Notification;
import com.lz.entity.AdminUserAuditLog;
import com.lz.mapper.UserMapper;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.NotificationMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.EventAdminMappingMapper;
import com.lz.mapper.AdminUserAuditLogMapper;
import com.lz.service.SportsImgService;
import com.lz.service.UserService;
import com.lz.service.NotificationService;
import com.lz.config.AppConfig;
import com.lz.dto.UserUpdateDTO;
import com.lz.vo.UserDetailVO;
import com.lz.vo.UserVO;
import com.lz.vo.chart.UserData;
import com.lz.vo.chart.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * User Service Implementation
 */
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.enums.NotificationType;
import com.lz.util.MailUtils;
import com.lz.util.RedisUtil;
import com.lz.util.StringUtils;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private SportsImgService sportsImgService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private EventAdminMappingMapper eventAdminMappingMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserAuditLogMapper adminUserAuditLogMapper;

    private static final long CODE_EXPIRE_MINUTES = 10;
    private static final long CODE_COOLDOWN_SECONDS = 60;
    private static final long VERIFY_TOKEN_EXPIRE_MINUTES = 30;
    private static final long LOGIN_TOKEN_EXPIRE_DAYS = 7;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO, String registerToken) {
        // 校验 registerToken 是否在黑名单中
        if (redisUtil.hasKey("blacklist:token:" + registerToken)) {
            throw new BusinessException("该注册令牌已失效，请重新验证");
        }

        // 解析 registerToken
        java.util.Map<String, Object> claims;
        try {
            claims = com.lz.util.JwtUtil.parseToken(registerToken, appConfig.getJwtKey());
            if (com.lz.util.JwtUtil.isExpired(registerToken, appConfig.getJwtKey())) {
                throw new BusinessException("注册令牌已过期");
            }
        } catch (Exception e) {
            throw new BusinessException("无效的注册令牌");
        }

        String tokenEmail = (String) claims.get("email");
        String verifyToken = (String) claims.get("verifyToken");

        if (!userRegisterDTO.getEmail().equals(tokenEmail)) {
            throw new BusinessException("注册邮箱与验证时的邮箱不一致");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userRegisterDTO.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // Check Email
        queryWrapper.clear();
        queryWrapper.eq("email", userRegisterDTO.getEmail());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setEmail(userRegisterDTO.getEmail());
        user.setVerifyToken(verifyToken);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        user.setUserType(UserRole.ATHLETE);
        user.setIsFirstLogin(false);
        user.setSchoolId(1L);
        
        userMapper.insert(user);

        // 将 registerToken 加入黑名单
        redisUtil.set("blacklist:token:" + registerToken, "1", 10, TimeUnit.MINUTES);
    }

    @Override
    public String sendVerifyCode(String email, String ip) {
        if (!email.endsWith("@qq.com")) {
            throw new BusinessException("仅支持QQ邮箱注册");
        }

        String rateLimitKey = "rate_limit:ip:" + ip;
        Long count = redisUtil.incr(rateLimitKey, 1);
        if (count == 1) {
            redisUtil.expire(rateLimitKey, 60, TimeUnit.SECONDS);
        }
        if (count > 3) {
            throw new BusinessException("请求过于频繁，请稍后再试", 429);
        }

        String code = MailUtils.generateCode();
        String verifyToken = UUID.randomUUID().toString().replace("-", "");

        String key = "verify_token:" + verifyToken;
        redisUtil.set(key, code + ":" + email, 5, TimeUnit.MINUTES);

        // 防撞库策略：不因邮箱是否真实存在或邮件是否投递成功而改变接口返回。
        // 仅记录日志，保持对调用方统一成功响应，避免泄露邮箱可用性。
        try {
            mailUtils.sendVerificationCodeMail(email, code);
        } catch (Exception ex) {
            log.warn("sendVerifyCode mail delivery skipped/failed for email=" + email);
        }

        return verifyToken;
    }

    @Override
    public String verifyCode(String verifyToken, String code) {
        String key = "verify_token:" + verifyToken;
        Object obj = redisUtil.get(key);
        if (obj == null) {
            throw new BusinessException("验证码错误或已过期");
        }

        String[] parts = obj.toString().split(":");
        if (parts.length != 2 || !parts[0].equals(code)) {
            throw new BusinessException("验证码错误");
        }

        String email = parts[1];
        redisUtil.del(key);

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("verifyToken", verifyToken);
        claims.put("email", email);
        return com.lz.util.JwtUtil.genToken(claims, appConfig.getJwtKey(), 10 * 60 * 1000L);
    }

    @Override
    public void auditUser(Long userId, Integer status, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (status == 1) {
            user.setStatus(UserStatus.ACTIVE);
            // 调用新的审核通知方法
            mailUtils.sendAuditResultMail(user.getEmail(), true, null);
        } else {
            user.setStatus(UserStatus.REJECTED);
            // 调用新的审核通知方法
            mailUtils.sendAuditResultMail(user.getEmail(), false, reason);
        }
        userMapper.updateById(user);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userLoginDTO.getUsername().contains("@")) {
            queryWrapper.eq("email", userLoginDTO.getUsername());
        } else {
            queryWrapper.eq("username", userLoginDTO.getUsername());
        }
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null || !passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }
        if (UserStatus.DISABLED == user.getStatus()) {
            throw new BusinessException("账号已禁用，请联系管理员");
        }
        if (UserStatus.ACTIVE != user.getStatus()) {
            throw new BusinessException("账号状态异常，请联系管理员");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO userUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsFirstLogin())
                && (userUpdateDTO.getNewPassword() == null || userUpdateDTO.getNewPassword().isBlank())) {
            throw new BusinessException("首次登录必须先修改密码", 403);
        }

        if (userUpdateDTO.getUserName() != null) {
            user.setUsername(userUpdateDTO.getUserName());
        }
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }

        // 校验是否有已通过的运动员认证
        boolean hasApprovedAthlete = athleteMapper.selectCount(
                new LambdaQueryWrapper<com.lz.entity.Athlete>()
                        .eq(com.lz.entity.Athlete::getUserId, userId)
                        .eq(com.lz.entity.Athlete::getAthleteState, com.lz.common.enums.AthleteStatus.APPROVED)
        ) > 0;

        if (userUpdateDTO.getName() != null) {
            if (hasApprovedAthlete && !userUpdateDTO.getName().equals(user.getName())) {
                throw new BusinessException("您已有认证通过的运动员记录，无法修改真实姓名");
            }
            user.setName(userUpdateDTO.getName());
        }
        if (userUpdateDTO.getGender() != null) {
            if (hasApprovedAthlete && !userUpdateDTO.getGender().equals(user.getGender())) {
                throw new BusinessException("您已有认证通过的运动员记录，无法修改性别");
            }
            user.setGender(userUpdateDTO.getGender());
        }
        if (userUpdateDTO.getDeptId() != null) {
            if (hasApprovedAthlete && !userUpdateDTO.getDeptId().equals(user.getDeptId())) {
                throw new BusinessException("您已有认证通过的运动员记录，无法修改所属部门");
            }
            user.setDeptId(userUpdateDTO.getDeptId());
        }
        if (userUpdateDTO.getContact() != null) {
            // 联系方式允许随时修改，或者根据业务需求也可以锁定。这里假设允许修改。
            user.setContact(userUpdateDTO.getContact());
        }

        if (userUpdateDTO.getNewPassword() != null) {
             if (userUpdateDTO.getOldPassword() == null
                     || !passwordEncoder.matches(userUpdateDTO.getOldPassword(), user.getPassword())) {
                 throw new BusinessException("旧密码错误");
             }
             user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
             user.setIsFirstLogin(false);
             redisUtil.del(buildUserLoginTokenKey(userId));
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(com.lz.dto.ResetPasswordDTO resetPasswordDTO) {
        String resetToken = resetPasswordDTO.getVerifyToken();
        String blacklistKey = "blacklist:reset-token:" + resetToken;
        if (redisUtil.hasKey(blacklistKey)) {
            throw new BusinessException("重置链接已使用，请重新获取", 400);
        }

        java.util.Map<String, Object> claims;
        try {
            claims = com.lz.util.JwtUtil.parseToken(resetToken, appConfig.getJwtKey());
            if (com.lz.util.JwtUtil.isExpired(resetToken, appConfig.getJwtKey())) {
                throw new BusinessException("重置链接已过期，请重新获取", 400);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("verifyToken无效或已过期", 400);
        }

        String tokenEmail = (String) claims.get("email");
        if (tokenEmail == null || !tokenEmail.equals(resetPasswordDTO.getEmail())) {
            throw new BusinessException("重置链接与邮箱不匹配", 400);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", resetPasswordDTO.getEmail());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("该邮箱未注册账号", 404);
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 重置成功后，使得已登录的token失效
        redisUtil.del(buildUserLoginTokenKey(user.getId()));

        // 一次性消费：重置链接使用后立即拉黑，禁止复用
        redisUtil.set(blacklistKey, "1", 10, TimeUnit.MINUTES);
    }

    @Override
    public PageResult list(EventListDTO listDto) {
        if (listDto.getPageSize() == 0) {
            listDto.setPageSize(10); // 默认每页10条
        }
        
        long currentPage = listDto.getCurrentPage() > 0 ? listDto.getCurrentPage() : 1;
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(currentPage, listDto.getPageSize());

        // 1. 创建 LambdaQueryWrapper
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 2. 添加查询条件
        // 用户名模糊查询
        if (StringUtils.hasText(listDto.getName())) {
            wrapper.like(User::getUsername, listDto.getName());
        }


        if (listDto.getType() != null && StringUtils.hasText(listDto.getType())) {
            // 如果数据库字段是 role
            wrapper.eq(User::getUserType, listDto.getType());
        }

        // 时间查询
        if (listDto.getDate() != null) {
            wrapper.gt(User::getCreateTime, listDto.getDate());
        }
        
        // 权限过滤（school_id 仅返回当前管理员所属学校数据）
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getSchoolId() != null) {
                wrapper.eq(User::getSchoolId, currentUser.getSchoolId());
            }
        }

        wrapper.orderByDesc(User::getCreateTime);
        baseMapper.selectPage(page, wrapper);
        
        List<User> users = page.getRecords();
        List<UserVO> list = users.stream()
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
        return new PageResult((int) page.getTotal(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id, String reason) {
        Long userId = Long.valueOf(id);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("删除失败，用户不存在");
        }
        if (user.getUserType() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("禁止删除超级管理员", 403);
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            return;
        }

        // 统一逻辑删除语义：仅变更账号状态，不做物理删除，避免历史报名/成绩/通知关联断裂。
        UserStatus beforeStatus = user.getStatus();
        user.setStatus(UserStatus.DISABLED);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        saveDeleteAuditLog(userId, beforeStatus, reason);

        // 逻辑删除后立刻使现有登录态失效，阻断后续操作。
        redisUtil.del(buildUserLoginTokenKey(userId));
    }

    private void saveDeleteAuditLog(Long targetUserId, UserStatus beforeStatus, String reason) {
        AdminUserAuditLog log = new AdminUserAuditLog();
        log.setOperatorId(BaseContext.getCurrentId());
        log.setTargetUserId(targetUserId);
        log.setAction("LOGICAL_DELETE_USER");
        log.setBeforeStatus(beforeStatus == null ? null : beforeStatus.name());
        log.setAfterStatus(UserStatus.DISABLED.name());
        log.setRemark(StringUtils.isBlank(reason) ? "管理员逻辑删除用户" : reason.trim());
        log.initTime();
        adminUserAuditLogMapper.insert(log);
    }


    @Override
    public UserData getUserNumsByMonth(String month) {
        UserData userData = new UserData();
        userData.setDate(month);
        userData.setAddUser(userMapper.getUserNumsByMonth(month));
        userData.setAddAthlete(athleteMapper.getAthleteNumsByMonth(month));
        return userData;
    }

    @Override
    public List<UserType> getUserTypes() {
        List<UserType> userTypes = new ArrayList<>();

        UserType userType1 = new UserType();
        Integer userNums = userMapper.getUserTotal();
        userType1.setType("学生");
        userType1.setNums(userNums != null ? userNums : 0);
        userTypes.add(userType1);

        // TODO: Get real data
        UserType userType2 = new UserType();
        Integer registrationNums = registrationMapper.getRegistrationPlayerTotal();
        if (registrationNums == null) registrationNums = 0;
        userType2.setType("已参加项目的运动员");
        userType2.setNums(registrationNums);
        userTypes.add(userType2);

        UserType userType3 = new UserType();
        Integer athleteNums = athleteMapper.getAthleteTotal();
        if (athleteNums == null) athleteNums = 0;
        userType3.setType("未参加项目的运动员");
        userType3.setNums(athleteNums - registrationNums);
        userTypes.add(userType3);

        return userTypes;
    }

    @Override
    public int[] getNums() {
        int[] nums = new int[6];
        Integer athleteTotal = athleteMapper.getAthleteTotal();
        nums[0] = athleteTotal != null ? athleteTotal : 0;
        nums[1] = eventMapper.getEventTotal();
        nums[2] = projectMapper.getProjectTotal();
        
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        
        nums[3] = athleteMapper.getAthleteNumByMonth(year, month);
        nums[4] = eventMapper.getEventNumsByMonth(year, month);
        nums[5] = projectMapper.getProjectNumsByMonth(year, month);
        
        return nums;
    }

    @Override
    public User selectUserInfo() {
        Long userId = BaseContext.getCurrentId();
        return getById(userId);
    }

    @Override
    public UserDetailVO getUserDetail() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String avatarImg = sportsImgService.selectImg(user.getId(), "avatar");
        if (avatarImg != null && !avatarImg.startsWith("http")) {
            avatarImg = "https://" + appConfig.getBucketName() + "." + appConfig.getEndpoint() + "/" + avatarImg;
        }

        return UserDetailVO.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType().getRole())
                .status(user.getStatus().getStatus())
                .registerTime(user.getCreateTime())
                .avatarSrc(avatarImg)
                .build();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false));
    }

    @Override
    public void saveLoginToken(Long userId, String token) {
        redisUtil.set(buildUserLoginTokenKey(userId), token, LOGIN_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    private String normalizeScene(String scene) {
        if (scene == null || scene.isBlank()) {
            return "REGISTER";
        }
        return scene.trim().toUpperCase();
    }

    private String buildCodeKey(String scene, String email) {
        return "code:" + scene + ":" + email;
    }

    private String buildVerifyTokenKey(String scene, String email) {
        return "verify_token:" + scene + ":" + email;
    }

    private String buildUserLoginTokenKey(Long userId) {
        return "auth:token:" + userId;
    }
}
