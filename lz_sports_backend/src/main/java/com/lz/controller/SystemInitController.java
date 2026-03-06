package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.dto.SchoolInitDTO;
import com.lz.service.SchoolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统初始化控制器
 * 负责系统的首次配置、管理员账号创建及状态检查
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Tag(name = "系统初始化", description = "系统状态检查与首次初始化接口")
public class SystemInitController {

    private final SchoolConfigService schoolConfigService;

    /**
     * 检查系统是否已初始化
     * 返回 true 表示系统已配置，false 表示需要进行初始化
     */
    @GetMapping("/init-status")
    @Operation(summary = "检查初始化状态", description = "检查系统是否已经完成首次初始化配置")
    public Result<Boolean> checkInit() {
        return Result.success(schoolConfigService.isInitialized());
    }

    /**
     * 执行系统初始化
     * 设置学校基本信息、创建首个管理员账号、配置初始年级/院系
     */
    @PostMapping("/init")
    @Operation(summary = "执行初始化", description = "提交学校信息及管理员资料进行系统初始化")
    public Result<String> initSystem(@RequestBody SchoolInitDTO schoolInitDTO) {
        log.info("初始化系统: {}", schoolInitDTO.getSchoolName());
        schoolConfigService.initSystem(schoolInitDTO);
        return Result.success("系统初始化成功");
    }
}
