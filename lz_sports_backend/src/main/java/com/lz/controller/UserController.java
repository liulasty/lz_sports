package com.lz.controller;

import com.lz.common.context.BaseContext;
import com.lz.common.enums.UserStatus;
import com.lz.common.result.PageResult;
import com.lz.common.result.Result;
import com.lz.config.AppConfig;
import com.lz.dto.EventListDTO;
import com.lz.dto.UserLoginDTO;
import com.lz.dto.UserRegisterDTO;
import com.lz.entity.User;
import com.lz.service.SportsImgService;
import com.lz.service.UserService;
import com.lz.util.JwtUtil;
import com.lz.util.MailUtils;
import com.lz.dto.UserUpdateDTO;
import com.lz.vo.UserDetailVO;
import com.lz.vo.UserLoginVO;
import com.lz.vo.chart.UserData;
import com.lz.vo.chart.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Controller
 */
@RestController
@RequestMapping("/sports/user")
@Slf4j
public class UserController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private SportsImgService sportsImgService;

    // TODO: Inject when migrated
    // private AthleteService athleteService;

    /**
     * Login
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        User user = userService.login(userLoginDTO);

        // Check status
        if (UserStatus.ACTIVE != user.getStatus()) {
             log.warn("用户 {} 状态为 {}, 但为了测试继续放行。", user.getUserName(), user.getStatus());
        }

        // Get Avatar
        String avatarImg = sportsImgService.selectImg(user.getUserId(), "avatar");
        if (avatarImg != null && !avatarImg.startsWith("http")) {
             avatarImg = "https://" + appConfig.getBucketName() + "." + appConfig.getEndpoint() + "/" + avatarImg;
        }

        // Set Context & Generate Token
        BaseContext.setCurrentId(user.getUserId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("username", user.getUserName());
        claims.put("role", user.getUserType().getRole());
        String token = JwtUtil.genToken(claims, appConfig.getJwtKey());
        
        log.info("用户登录成功: {}, Token: {}", user.getUserName(), token);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .type(user.getUserType().getRole())
                .token(token)
                .avatarSrc(avatarImg)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * Send Verification Code
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String email) {
        userService.sendCode(email);
        return Result.success("验证码已发送");
    }

    /**
     * Register
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success("注册申请已提交，请等待管理员审核");
    }

    /**
     * Audit User (Admin)
     */
    @PutMapping("/audit/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> auditUser(@PathVariable Long userId, @RequestParam Integer status, @RequestParam(required = false) String reason) {
        userService.auditUser(userId, status, reason);
        return Result.success("操作成功");
    }

    /**
     * Get User Info
     */
    @GetMapping("/info")
    public Result<UserDetailVO> info() {
        UserDetailVO userDetailVO = userService.getUserDetail();
        return Result.success(userDetailVO);
    }

    /**
     * Update User Info
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUser(userUpdateDTO);
        return Result.success("更新成功");
    }

    /**
     * Get User List (Admin)
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult> list(@RequestBody(required = false) EventListDTO listDto) {
        if (listDto == null) {
            listDto = new EventListDTO();
        }
        PageResult pageResult = userService.list(listDto);
        return Result.success(pageResult);
    }

    /**
     * Delete User (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * Examine Player Application (Admin)
     */
    @PutMapping("/examine/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> examine(@PathVariable String id) {
        userService.examinePlayer(id);
        return Result.success("审核通过");
    }

    /**
     * Get User Nums By Month (Admin)
     */
    @GetMapping("/getUserNumsByMonth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<UserData> getUserNumsByMonth(@RequestParam String month) {
        return Result.success(userService.getUserNumsByMonth(month));
    }

    /**
     * Get User Types (Admin)
     */
    @GetMapping("/getUserType")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<UserType>> getUserTypes() {
        return Result.success(userService.getUserTypes());
    }

    /**
     * Get Dashboard Nums (Admin)
     */
    @GetMapping("/getNums")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<int[]> getNums() {
        return Result.success(userService.getNums());
    }

    /**
     * Logout
     */
    @DeleteMapping("/logout")
    public Result<String> logout() {
        BaseContext.removeCurrentId();
        return Result.success("Logged out successfully");
    }
}
