package com.lz.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/16:57
 * @Description:
 */

import com.lz.Dao.AthleteDao;
import com.lz.Dao.UserDao;
import com.lz.Exception.NoAthleteException;
import com.lz.pojo.dto.AthleteDTO;
import com.lz.pojo.dto.AthleteUpdateDTO;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.entity.User;
import com.lz.service.AthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author lz
 */
@Service
public class AthleteServiceImpl implements AthleteService {
   

    @Autowired
    private AthleteDao athleteDao;
    
    @Autowired
    private UserDao userDao;
    /**
     * 运动员申请
     *
     * @param athleteDTO
     *
     * @return {@code String}
     */
    @Override
    public String add(AthleteDTO athleteDTO) {
        Athlete athlete = Athlete.builder().age(String.valueOf(athleteDTO.getAge()))
                .contact(athleteDTO.getPhone())
                .gender(athleteDTO.getGender())
                .applyTime(new Date())
                .name(athleteDTO.getName())
                .grade(athleteDTO.getGrade())
                .AthleteState("申请中")
                .userId(Long.valueOf(athleteDTO.getUserId()))
                .build();

        int insert = athleteDao.insert(athlete);
        return String.valueOf(insert);
    }

    /**
     * 查看申请
     *
     * @param id 编号
     *
     * @return {@code Athlete}
     */
    @Override
    public Athlete selectApply(Integer id) throws NoAthleteException{
        Athlete athlete = athleteDao.selectByUserId(id);
        if(athlete== null) {
            throw new NoAthleteException("查不到");
        }
        System.out.println("athlete:" + athlete);
        return athlete;
    }

    /**
     * 拒绝玩家
     *
     * @param id 编号
     */
    @Override
    public void refusePlayer(Integer id) {
        athleteDao.refusePlayer(id);
    }

    /**
     * 按用户 ID 删除
     *
     * @param id 编号
     */
    @Override
    public void deleteByUserId(Integer id) {
        athleteDao.deleteByUserId(id);
    }

    @Override
    public Athlete selectOne(Integer id) {

        return athleteDao.selectByUserId(id);
    }

    /**
     * 更新
     *
     * @param id               编号
     * @param athleteUpdateDTO 运动员更新 DTO
     */
    @Override
    public void update(Integer id, AthleteUpdateDTO athleteUpdateDTO) {
        Athlete athlete = athleteDao.selectById(id);
        athleteDao.deleteById(id);
        User user = userDao.selectById(athlete.getUserId());
        user.setUserType("学生");
        userDao.updateById(user);
        
        athlete.setApplyTime(new Date());
        athlete.setAgreeTime(null);
        
        athlete.setName(athleteUpdateDTO.getName());
        athlete.setAge(athlete.getAge());
        athlete.setGender(athlete.getGender());
        athlete.setGrade(athlete.getGrade());
        athlete.setContact(athlete.getContact());
        athlete.setAthleteState("申请中");
        athleteDao.insert(athlete);
        
    }


}