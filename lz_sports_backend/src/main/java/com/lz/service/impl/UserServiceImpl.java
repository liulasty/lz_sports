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
import com.lz.mapper.UserMapper;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.NotificationMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
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
    private PasswordEncoder passwordEncoder;

    private static final long CODE_EXPIRE_MINUTES = 10;
    private static final long CODE_COOLDOWN_SECONDS = 60;
    private static final long VERIFY_TOKEN_EXPIRE_MINUTES = 30;
    private static final long LOGIN_TOKEN_EXPIRE_DAYS = 7;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO) {
        String verifyTokenKey = buildVerifyTokenKey("REGISTER", userRegisterDTO.getEmail());
        Object verifyTokenObj = redisUtil.get(verifyTokenKey);
        if (verifyTokenObj == null || !userRegisterDTO.getVerifyToken().equals(verifyTokenObj.toString())) {
            throw new BusinessException("verifyToken无效或已过期");
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
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        user.setUserType(UserRole.ATHLETE);
        user.setIsFirstLogin(false);
        user.setSchoolId(1L);
        
        userMapper.insert(user);

        redisUtil.del(verifyTokenKey, buildCodeKey("REGISTER", userRegisterDTO.getEmail()));
    }

    @Override
    public void sendCode(String email, String scene) {
        String normalizedScene = normalizeScene(scene);
        if (!email.endsWith("@qq.com")) {
            throw new BusinessException("仅支持QQ邮箱注册");
        }

        if ("REGISTER".equals(normalizedScene)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("该邮箱已注册");
            }
        }

        String key = buildCodeKey(normalizedScene, email);
        long expire = redisUtil.getExpire(key, TimeUnit.SECONDS);
        long threshold = CODE_EXPIRE_MINUTES * 60 - CODE_COOLDOWN_SECONDS;
        if (expire > threshold) {
            throw new BusinessException("发送过于频繁，请" + (expire - threshold) + "秒后重试", 409);
        }

        String code = MailUtils.generateCode();
        redisUtil.set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        mailUtils.sendVerificationCodeMail(email, code);
    }

    @Override
    public String verifyCode(String email, String code, String scene) {
        String normalizedScene = normalizeScene(scene);
        String codeKey = buildCodeKey(normalizedScene, email);
        Object codeObj = redisUtil.get(codeKey);
        if (codeObj == null || !code.equals(codeObj.toString())) {
            throw new BusinessException("验证码错误或已过期");
        }

        String verifyToken = UUID.randomUUID().toString().replace("-", "");
        String verifyTokenKey = buildVerifyTokenKey(normalizedScene, email);
        redisUtil.set(verifyTokenKey, verifyToken, VERIFY_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisUtil.del(codeKey);
        return verifyToken;
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

        if (userUpdateDTO.getUserName() != null) {
            user.setUsername(userUpdateDTO.getUserName());
        }
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
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
    public PageResult list(EventListDTO listDto) {
        if (listDto.getPageSize() == 0) {
            listDto.setPageSize(10); // 默认每页10条
        }
        // 计算分页偏移量
        long currentPage = listDto.getCurrentPage();
        if (currentPage > 0) {
            listDto.setCurrentPage((currentPage - 1) * listDto.getPageSize());
        }

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

            // 如果数据库字段是 userType
            // wrapper.eq(SysUser::getUserType, listDto.getType());
        }

        // 时间查询
        if (listDto.getDate() != null) {
            wrapper.gt(User::getCreateTime, listDto.getDate());
        }

        wrapper.orderByDesc(User::getCreateTime);
        List<User> users = baseMapper.selectList(wrapper);
        List<UserVO> list = users.stream()
                .map(User::toUserVO)
                .collect(Collectors.toList());
        int total = userMapper.getTotalUserCount(listDto);
        return new PageResult(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        // Check if athlete exists
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Athlete::getUserId, Long.valueOf(id));
        if (athleteMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("删除失败，请删除相关信息");
        }

        int delete = userMapper.deleteById(id);
        if (delete == 0) {
            throw new BusinessException("删除失败，用户不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void examinePlayer(String id) {
        Athlete current = athleteMapper.selectOne(new LambdaQueryWrapper<Athlete>().eq(Athlete::getUserId, Long.valueOf(id)));
        if (current == null) {
            throw new BusinessException("运动员申请不存在");
        }
        if (current.getAthleteState() != AthleteStatus.AUDITING) {
            throw new BusinessException("已审核的申请不可重复审核");
        }
        User user = new User();
        user.setId(Long.valueOf(id));
        user.setUserType(UserRole.ATHLETE);
        userMapper.updateById(user);

        // Update Athlete status
        Athlete athlete = new Athlete();
        athlete.setAgreeTime(LocalDateTime.now());
        athlete.setAthleteState(AthleteStatus.SUCCESS);
        
        LambdaQueryWrapper<Athlete> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Athlete::getUserId, Long.valueOf(id));
        athleteMapper.update(athlete, updateWrapper);
        notificationService.create(Long.valueOf(id), "运动员审核结果", "您的运动员申请已审核通过", NotificationType.ATHLETE_APPROVED);
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
