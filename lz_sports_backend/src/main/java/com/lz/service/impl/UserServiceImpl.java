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
import com.lz.mapper.UserMapper;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.service.SportsImgService;
import com.lz.service.UserService;
import com.lz.config.AppConfig;
import com.lz.dto.UserUpdateDTO;
import com.lz.vo.UserDetailVO;
import com.lz.vo.UserVO;
import com.lz.vo.chart.UserData;
import com.lz.vo.chart.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Service Implementation
 */
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.util.MailUtils;
import com.lz.util.RedisUtil;

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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO) {
        // Check verification code
        String code = (String) redisUtil.get("REGISTER_CODE:" + userRegisterDTO.getEmail());
        if (code == null || !code.equals(userRegisterDTO.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // Check Username
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("Username", userRegisterDTO.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // Check Email
        queryWrapper.clear();
        queryWrapper.eq("Email", userRegisterDTO.getEmail());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUserName(userRegisterDTO.getUsername());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        user.setRegisterTime(LocalDateTime.now());
        user.setStatus(UserStatus.PENDING); // Set to PENDING
        user.setUserType(UserRole.USER);
        
        userMapper.insert(user);
        
        // Remove code from redis
        redisUtil.del("REGISTER_CODE:" + userRegisterDTO.getEmail());
    }

    @Override
    public void sendCode(String email) {
        // Verify QQ email
        if (!email.endsWith("@qq.com")) {
            throw new BusinessException("仅支持QQ邮箱注册");
        }
        
        // Check if email registered
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("Email", email);
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("该邮箱已注册");
        }

        String code = MailUtils.generateCode();
        redisUtil.set("REGISTER_CODE:" + email, code, 300); // 5 minutes
        
        mailUtils.sendMail(email, "注册验证码", "您的验证码是: " + code + "，有效期5分钟。");
    }

    @Override
    public void auditUser(Long userId, Integer status, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (status == 1) {
            user.setStatus(UserStatus.ACTIVE);
            mailUtils.sendMail(user.getEmail(), "账号审核通过", "您的账号已通过审核，现在可以登录系统了。");
        } else {
            user.setStatus(UserStatus.REJECTED);
            mailUtils.sendMail(user.getEmail(), "账号审核拒绝", "很遗憾，您的账号审核未通过。原因: " + reason);
        }
        userMapper.updateById(user);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userLoginDTO.getUsername().contains("@")) {
            queryWrapper.eq("Email", userLoginDTO.getUsername());
        } else {
            queryWrapper.eq("Username", userLoginDTO.getUsername());
        }
        queryWrapper.eq("Password", userLoginDTO.getPassword());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
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
            user.setUserName(userUpdateDTO.getUserName());
        }
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getNewPassword() != null) {
             if (userUpdateDTO.getOldPassword() == null || !user.getPassword().equals(userUpdateDTO.getOldPassword())) {
                 throw new BusinessException("旧密码错误");
             }
             user.setPassword(userUpdateDTO.getNewPassword());
        }

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

        List<UserVO> list = userMapper.selectAllAndState(listDto);
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
        User user = new User();
        user.setUserId(Long.valueOf(id));
        user.setUserType(UserRole.ATHLETE);
        userMapper.updateById(user);

        // Update Athlete status
        Athlete athlete = new Athlete();
        athlete.setAgreeTime(LocalDateTime.now());
        athlete.setAthleteState(AthleteStatus.SUCCESS);
        
        LambdaQueryWrapper<Athlete> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Athlete::getUserId, Long.valueOf(id));
        athleteMapper.update(athlete, updateWrapper);
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

        String avatarImg = sportsImgService.selectImg(user.getUserId(), "avatar");
        if (avatarImg != null && !avatarImg.startsWith("http")) {
            avatarImg = "https://" + appConfig.getBucketName() + "." + appConfig.getEndpoint() + "/" + avatarImg;
        }

        return UserDetailVO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .userType(user.getUserType().getRole())
                .status(user.getStatus().getStatus())
                .registerTime(user.getRegisterTime())
                .avatarSrc(avatarImg)
                .build();
    }
}
