package com.lz.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/16:49
 * @Description:
 */

import com.lz.Exception.NoAthleteException;
import com.lz.pojo.dto.AthleteDTO;
import com.lz.pojo.dto.AthleteUpdateDTO;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.result.Result;
import com.lz.service.AthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lz
 */
@RestController
@RequestMapping("sports/athlete")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AthleteController {
    
    @Autowired
    private AthleteService athleteService;

    /**
     * 提交申请
     *
     * @param athleteDTO 运动员 DTO
     *
     * @return {@code Result}
     */
    @PostMapping
    public Result add(@RequestBody AthleteDTO athleteDTO){
        
        athleteService.add(athleteDTO);
        
        return Result.success("申请已提交");
    }

    /**
     * 查询运动员申请
     *
     * @param id 编号
     *
     * @return {@code Result<Athlete>}
     */
    @GetMapping("/apply/{id}")
    public Result<Athlete> selectApply(@PathVariable Integer id) throws NoAthleteException {
        
        Athlete athlete = athleteService.selectApply(id);
        
        return Result.success(athlete);
    }

    /**
     * 查询运动员信息
     *
     * @param id 编号
     *
     * @return {@code Result<Athlete>}
     */
    @GetMapping("/{id}")
    public Result<Athlete> selectAthlete(@PathVariable Integer id) throws NoAthleteException {

        Athlete athlete = athleteService.selectOne(id);

        return Result.success(athlete);
    }
    
    @PutMapping("/{id}")
    public Result<String> updateAthlete(@PathVariable Integer id,
                                        @RequestBody AthleteUpdateDTO athleteUpdateDTO){
        athleteService.update(id,athleteUpdateDTO);
        
        return Result.success("更新成功");
    }
    
    

    /**
     * 删除记录
     *
     * @param id 编号
     *
     * @return {@code Result<String>}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@PathVariable Integer id){
        athleteService.deleteByUserId(id);
        
        return Result.success("删除成功");
    }
}