package com.lz.controller;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lz.Dao.AthleteDao;
import com.lz.context.BaseContext;
import com.lz.pojo.dto.RegistrationAndAthleteDTO;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.Result;
import com.lz.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lz
 */
@Slf4j
@RestController
@RequestMapping("sports/registration")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AthleteDao athleteDao;

    /**
     * 分页查询列表
     *
     * @param name        名字
     * @param status      地位
     * @param date        日期
     * @param currentPage 当前页面
     * @param pageSize    页面大小
     *
     * @return {@code Result<PageResult>}
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {

        System.out.println("date:" + date);
        Long uid = BaseContext.getCurrentId();
        QueryWrapper<Athlete> athleteQW = Wrappers.query();
        athleteQW.eq("UserID", uid);
        Athlete athlete = athleteDao.selectOne(athleteQW);
        PageResult registrationList = new PageResult();
        if(athlete == null){
            System.out.println("管理员");
            
            registrationList = registrationService.list(currentPage,
                                                        pageSize, name,
                                                        status,
                                                        stringToData(date));
        }else {
            registrationList = registrationService.listByPlayer(currentPage,
                                                        pageSize, name,
                                                        status,
                                                        stringToData(date),
                                                                athlete.getAthleteId());
        }
       

        return Result.success(registrationList);
    }

    /**
     * 参赛记录查询
     *
     * @param id 编号
     *
     * @return {@code Result<RegistrationAndAthleteDTO>}
     */
    @GetMapping("/{id}")
    public Result<RegistrationAndAthleteDTO> registrationQuery(@PathVariable Long id){
        RegistrationAndAthleteDTO registrationAndAthleteDTO =
                registrationService.selectOne(id);
        return Result.success(registrationAndAthleteDTO);
    }

    /**
     * 获取运动员参加总数
     *
     * @param id 编号
     *
     * @return {@code Result<Integer>}
     */
    @GetMapping("/athlete/{id}")
    public Result<Integer> getAthleteRegistrationTotal(@PathVariable Long id){
        int total= registrationService.getRegistrationTotalByAthlete(id);
        
        return Result.success(total);
    }

    /**
     * 删除
     *
     * @param id 编号
     *
     * @return {@code Result<String>}
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        Result<String> result =registrationService.delete(id);
        return result;
    }

    /**
     * 同意参加项目
     *
     * @param id 编号
     *
     * @return {@code Result<String>}
     */
    @PutMapping("/{id}")
    public Result<String> attend(@PathVariable Long id){
        registrationService.attend(id);
        
        return Result.success("同意参加");
    }

    /**
     * 拒绝申请
     *
     * @param id 编号
     *
     * @return {@code Result<String>}
     */
    @PutMapping("/refuse/{id}")
    public Result<String> refuse(@PathVariable Long id){
        registrationService.refuse(id);

        return Result.success("驳回申请");
    }

    public Date stringToData(String s){

        if(s == null || "".equals(s)){
            return null;
        }
        Date date;
        try {
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return date;
    }
}