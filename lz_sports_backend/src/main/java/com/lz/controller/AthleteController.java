package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.dto.AthleteDTO;
import com.lz.dto.AthleteUpdateDTO;
import com.lz.entity.Athlete;
import com.lz.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运动员管理控制器
 * 处理运动员的申请、查询、更新和删除等操作
 */
@RestController
@RequestMapping("/api/athlete")
@RequiredArgsConstructor
@Tag(name = "运动员管理", description = "运动员申请、信息查询与管理接口")
public class AthleteController {

    private final AthleteService athleteService;

    /**
     * 查询我的所有申请
     * 返回当前用户的所有赛事运动员申请记录
     */
    @GetMapping("/my-applications")
    @Operation(summary = "我的申请列表", description = "获取当前用户的所有运动员申请记录")
    public Result<java.util.List<Athlete>> getMyApplications() {
        return Result.success(athleteService.getMyApplications());
    }

    /**
     * 提交运动员申请
     * 允许普通用户提交成为运动员的申请信息，需管理员审核
     */
    @PostMapping
    @Operation(summary = "提交申请", description = "提交成为运动员的申请信息")
    public Result<String> add(@Valid @RequestBody AthleteDTO athleteDTO) {
        athleteService.add(athleteDTO);
        return Result.success("申请已提交");
    }

    /**
     * 查询申请状态
     * 根据用户ID和赛事ID查询其当前的运动员申请状态
     */
    @GetMapping("/apply/{id}")
    @Operation(summary = "查询申请状态", description = "根据用户ID查询申请记录")
    public Result<Athlete> selectApply(@Parameter(description = "用户ID") @PathVariable Long id, @RequestParam(required = false) Long eventId) {
        return Result.success(athleteService.selectApply(id, eventId));
    }

    /**
     * 查询运动员详情
     * 优先根据运动员ID查询，若未找到则尝试作为用户ID查询申请记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询运动员详情", description = "根据运动员ID或用户ID获取详细信息")
    public Result<Athlete> selectAthlete(@Parameter(description = "运动员ID或用户ID") @PathVariable Long id, @RequestParam(required = false) Long eventId) {
        Athlete athlete = athleteService.selectOne(id);
        if (athlete == null) {
            // 兼容逻辑：如果找不到运动员ID，尝试作为用户ID查询申请记录
            try {
                athlete = athleteService.selectApply(id, eventId);
            } catch (Exception e) {
                // 忽略异常，返回null或抛出业务异常由全局处理
            }
        }
        return Result.success(athlete);
    }

    /**
     * 更新运动员信息
     * 用户重新提交申请或更新资料时调用
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新信息", description = "更新运动员申请信息")
    public Result<String> updateAthlete(
            @Parameter(description = "运动员ID") @PathVariable Long id, 
            @Valid @RequestBody AthleteUpdateDTO athleteUpdateDTO) {
        athleteService.update(id, athleteUpdateDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除申请记录
     * 用户撤销申请或管理员删除记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录", description = "根据运动员ID取消申请记录")
    public Result<String> deleteRecord(@Parameter(description = "运动员ID") @PathVariable Long id) {
        athleteService.cancelApplication(id);
        return Result.success("删除成功");
    }
}
