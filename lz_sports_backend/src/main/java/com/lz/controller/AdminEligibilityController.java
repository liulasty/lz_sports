package com.lz.controller;

import com.lz.common.annotation.RequireRole;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.Result;
import com.lz.dto.EligibilityConfigDTO;
import com.lz.entity.Project;
import com.lz.mapper.ProjectMapper;
import com.lz.service.EligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资格规则管理接口（管理员）。
 */
@RestController
@RequestMapping("/api/admin/project")
@RequiredArgsConstructor
@Tag(name = "资格规则管理", description = "管理员配置项目资格规则")
public class AdminEligibilityController {

    private final EligibilityService eligibilityService;
    private final ProjectMapper projectMapper;

    @GetMapping("/{itemId}/eligibility")
    @Operation(summary = "获取资格规则", description = "获取指定项目的完整规则配置")
    public Result<EligibilityConfigDTO> getConfig(@PathVariable Long itemId) {
        assertProjectExists(itemId);
        EligibilityConfigDTO config = eligibilityService.getConfig(itemId);
        return Result.success(config);
    }

    @PutMapping("/{itemId}/eligibility")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "保存资格规则", description = "全量覆盖保存规则配置，清空旧规则后插入新规则")
    public Result<String> saveConfig(@PathVariable Long itemId, @RequestBody EligibilityConfigDTO dto) {
        assertProjectExists(itemId);
        eligibilityService.saveConfig(itemId, dto);
        return Result.success("规则配置已保存");
    }

    @DeleteMapping("/{itemId}/eligibility")
    @RequireRole({UserRole.SCHOOL_ADMIN})
    @Operation(summary = "清空资格规则", description = "清空后该项目全体可报")
    public Result<String> deleteConfig(@PathVariable Long itemId) {
        assertProjectExists(itemId);
        eligibilityService.deleteConfig(itemId);
        return Result.success("规则已清空");
    }

    private void assertProjectExists(Long itemId) {
        if (projectMapper.selectById(itemId) == null) {
            throw new BusinessException("项目不存在");
        }
    }
}
