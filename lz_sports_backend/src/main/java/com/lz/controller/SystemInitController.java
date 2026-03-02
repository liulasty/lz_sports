package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.dto.SchoolInitDTO;
import com.lz.service.SchoolConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/sports/init")
@RequiredArgsConstructor
public class SystemInitController {

    private final SchoolConfigService schoolConfigService;

    /**
     * Check if system is initialized
     */
    @GetMapping("/check")
    public Result<Boolean> checkInit() {
        return Result.success(schoolConfigService.isInitialized());
    }

    /**
     * Initialize system
     */
    @PostMapping
    public Result<String> initSystem(@RequestBody SchoolInitDTO schoolInitDTO) {
        log.info("初始化系统: {}", schoolInitDTO.getSchoolName());
        schoolConfigService.initSystem(schoolInitDTO);
        return Result.success("系统初始化成功");
    }
}
