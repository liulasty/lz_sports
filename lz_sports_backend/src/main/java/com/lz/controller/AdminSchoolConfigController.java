package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.result.Result;
import com.lz.dto.SchoolConfigUpdateDTO;
import com.lz.service.SchoolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/school-config")
@RequiredArgsConstructor
@Tag(name = "管理端-学校配置", description = "学校个性化配置接口")
public class AdminSchoolConfigController {

    private final SchoolConfigService schoolConfigService;

    @PutMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "更新学校配置", description = "更新学校名称、主题色等")
    public Result<Void> updateConfig(@RequestBody SchoolConfigUpdateDTO dto) {
        schoolConfigService.updateSchoolConfig(dto);
        return Result.success();
    }

    @PostMapping("/logo")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN})
    @Operation(summary = "上传学校Logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        return Result.success(schoolConfigService.uploadLogo(file));
    }
}
