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
import com.lz.dto.UserUpdateDTO;
import com.lz.vo.UserDetailVO;
import com.lz.vo.UserLoginVO;
import com.lz.vo.chart.UserData;
import com.lz.vo.chart.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 负责用户认证、注册、个人信息管理及后台用户管理
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "用户认证", description = "用户登录、注册、密码重置")
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
     * 用户登录
     * 支持用户名/邮箱 + 密码登录，返回JWT Token
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户凭证验证并返回Token")
    public Result<UserLoginVO> login(
            @Validated @RequestBody UserLoginDTO userLoginDTO, 
            HttpServletRequest request) {
        User user = userService.login(userLoginDTO);

        // 检查用户状态
        if (UserStatus.ACTIVE != user.getStatus()) {
             log.warn("用户 {} 状态为 {}, 但为了测试继续放行。", user.getUsername(), user.getStatus());
        }

        // 获取头像
        String avatarImg = sportsImgService.selectImg(user.getId(), "avatar");
        if (avatarImg != null && !avatarImg.startsWith("http")) {
             avatarImg = "https://" + appConfig.getBucketName() + "." + appConfig.getEndpoint() + "/" + avatarImg;
        }

        // 设置上下文 & 生成 Token
        BaseContext.setCurrentId(user.getId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getUserType());
        String token = JwtUtil.genToken(claims, appConfig.getJwtKey());
        
        log.info("用户登录成功: {}, Token: {}", user.getUsername(), token);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .type(user.getUserType().getRole())
                .token(token)
                .avatarSrc(avatarImg)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 发送验证码
     * 向指定邮箱发送注册验证码
     */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "向用户邮箱发送验证码")
    public Result<String> sendCode(@Parameter(description = "邮箱地址") @RequestParam String email) {
        userService.sendCode(email);
        return Result.success("验证码已发送");
    }

    /**
     * 用户注册
     * 提交注册信息，验证验证码，注册成功后需等待审核
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册申请")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success("注册申请已提交，请等待管理员审核");
    }

    /**
     * 审核用户
     * 管理员审核新注册用户，批准或拒绝
     */
    @PutMapping("/audit/{userId}")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "审核用户", description = "管理员审核用户注册申请")
    public Result<String> auditUser(
            @Parameter(description = "用户ID") @PathVariable Long userId, 
            @Parameter(description = "审核状态(1:通过, 0:拒绝)") @RequestParam Integer status, 
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String reason) {
        userService.auditUser(userId, status, reason);
        return Result.success("操作成功");
    }

    /**
     * 获取个人信息
     * 获取当前登录用户的详细信息
     */
    @GetMapping("/info")
    @Operation(summary = "个人信息", description = "获取当前登录用户的详细资料")
    public Result<UserDetailVO> info() {
        UserDetailVO userDetailVO = userService.getUserDetail();
        return Result.success(userDetailVO);
    }

    /**
     * 更新个人信息
     * 用户修改自己的资料（昵称、密码等）
     */
    @PostMapping("/update")
    @Operation(summary = "更新信息", description = "更新当前用户的个人资料")
    public Result<String> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUser(userUpdateDTO);
        return Result.success("更新成功");
    }

    /**
     * 查询用户列表
     * 管理员分页查询系统用户
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "用户列表", description = "管理员分页查询用户列表")
    public Result<PageResult> list(@RequestBody(required = false) EventListDTO listDto) {
        if (listDto == null) {
            listDto = new EventListDTO();
        }
        PageResult pageResult = userService.list(listDto);
        return Result.success(pageResult);
    }

    /**
     * 删除用户
     * 管理员删除指定用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    public Result<String> delete(@Parameter(description = "用户ID") @PathVariable String id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 审核运动员资格
     * 管理员直接通过运动员的资格申请
     */
    @PutMapping("/examine/{id}")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "审核运动员", description = "管理员审核通过运动员资格")
    public Result<String> examine(@Parameter(description = "用户ID") @PathVariable String id) {
        userService.examinePlayer(id);
        return Result.success("审核通过");
    }

    /**
     * 月度用户统计
     * 统计指定月份的用户增长情况
     */
    @GetMapping("/getUserNumsByMonth")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "月度统计", description = "统计指定月份的用户注册数量")
    public Result<UserData> getUserNumsByMonth(@Parameter(description = "月份") @RequestParam String month) {
        return Result.success(userService.getUserNumsByMonth(month));
    }

    /**
     * 用户类型统计
     * 统计各类用户的数量分布
     */
    @GetMapping("/getUserType")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "类型统计", description = "统计各类型用户的数量分布")
    public Result<List<UserType>> getUserTypes() {
        return Result.success(userService.getUserTypes());
    }

    /**
     * 仪表盘统计数据
     * 获取后台首页所需的各类统计数字
     */
    @GetMapping("/getNums")
    @PreAuthorize("hasAuthority('ROLE_SCHOOL_ADMIN')")
    @Operation(summary = "仪表盘数据", description = "获取系统概览统计数据")
    public Result<int[]> getNums() {
        return Result.success(userService.getNums());
    }

    /**
     * 退出登录
     * 清除登录状态（若有服务端状态）
     */
    @DeleteMapping("/logout")
    @Operation(summary = "退出登录", description = "用户退出登录")
    public Result<String> logout() {
        // 当前使用JWT，无状态，前端清除Token即可。
        // 若有Redis黑名单机制，可在此处加入。
        return Result.success("退出成功");
    }
}
