package com.lz.controller;



import com.lz.pojo.entity.SportsImg;
import com.lz.service.SportsImgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lz.Dao.UserDao;
import com.lz.config.AppConfig;
import com.lz.context.BaseContext;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.dto.UserLoginDTO;
import com.lz.pojo.dto.UserRegisterDTO;
import com.lz.pojo.entity.User;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.Result;
import com.lz.pojo.result.chart.UserType;
import com.lz.pojo.vo.UserLoginVO;
import com.lz.service.AthleteService;
import com.lz.service.UserService;
import com.lz.utils.JwtUtil;
import com.lz.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lz
 */
@RestController
@RequestMapping("/sports/user")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Slf4j
public class UserController {
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private AthleteService athleteService;

    @Autowired
    private SportsImgService sportsImgService;

    /**
     * 验证
     *
     * @param result 结果
     *
     * @return {@code String}
     */
    public String validate(BindingResult result) {
        // 异常防护：检查result是否为null
        if (result == null) {
            throw new IllegalArgumentException("BindingResult cannot be null");
        }

        List<FieldError> fieldErrors = result.getFieldErrors();

        // 添加分隔符：使用 Collectors.joining() 方法拼接错误消息，以换行符分隔
        String errorMessages = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        if (!fieldErrors.isEmpty()) {
            return errorMessages;
        }
        return null;
    }

    /**
     * 登录
     *
     * @param userLoginDTO 用户登录 DTO
     *
     * @return {@code Result<UserLoginVO>}
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request,
                                     BindingResult result) {
        //校验结果
        if (validate(result) != null) {
            return Result.error(validate(result));
        }



        
        try {
            User user = userService.login(userLoginDTO);
            
            // 检查用户状态
            if (!"已激活".equals(user.getStatus())) {
                String activeUrl =
                        "http://localhost:80/sports/user/active/" + user.getUserId();
                sendActivationEmail(user.getEmail(), activeUrl);
                return Result.error("请激活你的账户");
            }
            // 登录成功
            String avatarImg  = sportsImgService.selectImg(user.getUserId(),
                                                   "avatar");
            avatarImg ="https://"+ appConfig.getBucketName() + "."
                    + appConfig.getEndpoint() + "/" + avatarImg;
            BaseContext.setCurrentId(user.getUserId());
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("id", user.getUserId());
            claims.put("username", user.getUserName());
            String token = JwtUtil.genToken(claims, appConfig.getJwtKey());
            log.info("token: {}", token);

            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getUserId())
                    .userName(user.getUserName())
                    .type(user.getUserType())
                    .token(token)
                    .avatarSrc(avatarImg)
                    .build();
            String ipAddress = request.getRemoteAddr();
            log.info("客户端IP地址：{}", ipAddress);
            return Result.success(userLoginVO);
        } catch (Exception e) {
            // 统一异常处理（在实际项目中，此处应改为调用全局异常处理器）
            log.error("登录过程中发生异常", e);
            return Result.error("登录失败，请稍后重试");
        }


        
    }

    private void sendActivationEmail(String email, String activationUrl) {
        String subject = "账户激活邮件";
        String content = "你好，这是一封激活邮件，无需回复，点击此链接激活：" + activationUrl;
        MailUtils.sendMail(email, subject, content);
    }

    /**
     * 注销
     *
     * @return {@code Result}
     */
    @DeleteMapping("/logout")
    public Result<Object> logout() {
        Long currentId = BaseContext.getCurrentId();
        String s = currentId.toString();
        BaseContext.removeCurrentId();
        if (!"0".equals(s)) {
            BaseContext.removeCurrentId();
            return Result.success("登出");
        }


        return Result.error("未知错误");
    }

    /**
     * 注册
     *
     * @param userRegisterDTO 用户注册 DTO
     * @param result          结果
     *
     * @return {@code Result}
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO, BindingResult result) {
        //校验结果
        if (validate(result) != null) {
            return Result.error(validate(result));
        }
        
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("Username", userRegisterDTO.getUsername());
            User user = userDao.selectOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setUserName(userRegisterDTO.getUsername());
                user.setPassword(userRegisterDTO.getPassword());
                user.setUserType("学生");
                user.setStatus("未启用");
                user.setEmail(userRegisterDTO.getEmail());
                user.setRegisterTime(new Date());
                userDao.insert(user);
                User userActive = userDao.selectOne(queryWrapper);
                String activeUrl =
                        "http://localhost:80/sports/user/active/" + userActive.getUserId();
                MailUtils.sendMail(user.getEmail(),
                                   "你好，这是一封激活邮件，无需回复，点击此链接激活" + activeUrl,
                                   "测试邮件");
                return Result.success("请在你的邮箱点击激活链接");
            } else {
                return Result.error("名字重复了，请换个试试");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return Result.error(("未知错误"));
    }

    /**
     * 激活用户
     *
     * @param userId 用户 ID
     *
     * @return {@code Result}
     */
    @GetMapping("/active/{userId}")
    public Result<Object> activeUser(@PathVariable Long userId) {
        
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("UserID", userId);
            User user = userDao.selectOne(queryWrapper);

            if (user == null) {
                throw new Exception();
            }

            String status = user.getStatus();

            System.out.println("status:" + status);

            user.setStatus("已激活");
            int update = userDao.updateById(user);

            return Result.success("激活成功");
        } catch (Exception e) {
            e.printStackTrace();
        }


        return Result.error("激活失败,请联系管理员");
    }

    @GetMapping("/checkLogin")
    public Result<User> checkLogin(HttpServletRequest request) {
        Long currentId = BaseContext.getCurrentId();

        String ipAddress = request.getRemoteAddr();
        System.out.println("客户端IP地址：" + ipAddress);
        User user = userService.selectUserInfo();

        return Result.success(user, "已经登录");
    }


    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        EventListDto listDto = new EventListDto(name, type, date, offset, pageSize);
        PageResult list = userService.list(listDto);

        return Result.success(list);
    }

    /**
     * 删除用户
     *
     * @param id 编号
     *
     * @return {@code String}
     */
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id) throws SQLException {
        userService.delete(id);

        return "删除成功";
    }

    @GetMapping("/athlete/{id}")
    public Result<Object> examinePlayer(@PathVariable String id) {
        userService.examinePlayer(id);

        return Result.success("修改成功");
    }

    /**
     * 拒绝申请运动员
     *
     * @param id 编号
     *
     * @return {@code Result}
     */
    @DeleteMapping("/athlete/{id}")
    public Result<Object> refusePlayer(@PathVariable Integer id) {
        athleteService.refusePlayer(id);
        return Result.success("拒绝成功");
    }

}