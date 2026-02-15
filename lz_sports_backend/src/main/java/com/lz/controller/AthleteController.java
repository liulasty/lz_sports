package com.lz.controller;

import com.lz.common.result.Result;
import com.lz.dto.AthleteDTO;
import com.lz.dto.AthleteUpdateDTO;
import com.lz.entity.Athlete;
import com.lz.service.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Athlete Controller
 */
@RestController
@RequestMapping("/sports/athlete")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    /**
     * Submit application
     */
    @PostMapping
    public Result<String> add(@RequestBody AthleteDTO athleteDTO) {
        athleteService.add(athleteDTO);
        return Result.success("申请已提交");
    }

    /**
     * Get application by User ID
     */
    @GetMapping("/apply/{id}")
    public Result<Athlete> selectApply(@PathVariable Long id) {
        return Result.success(athleteService.selectApply(id));
    }

    /**
     * Get Athlete Info
     * Tries to find by AthleteID first.
     */
    @GetMapping("/{id}")
    public Result<Athlete> selectAthlete(@PathVariable Long id) {
        Athlete athlete = athleteService.selectOne(id);
        if (athlete == null) {
            // Fallback: Check if it's a UserID (for compatibility or convenience)
            try {
                athlete = athleteService.selectApply(id);
            } catch (Exception e) {
                // Ignore, return null or throw original error if desired
            }
        }
        return Result.success(athlete);
    }

    /**
     * Update Athlete Info (Re-apply)
     * @param id AthleteID
     */
    @PutMapping("/{id}")
    public Result<String> updateAthlete(@PathVariable Long id, @RequestBody AthleteUpdateDTO athleteUpdateDTO) {
        athleteService.update(id, athleteUpdateDTO);
        return Result.success("更新成功");
    }

    /**
     * Delete Athlete Record by User ID
     * @param id UserID
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@PathVariable Long id) {
        athleteService.deleteByUserId(id);
        return Result.success("删除成功");
    }
}
