package com.lz.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/05/16:42
 * @Description:
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lz.Dao.*;
import com.lz.context.BaseContext;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.dto.UserLoginDTO;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.entity.User;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.chart.UserData;
import com.lz.pojo.result.chart.UserType;
import com.lz.pojo.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lz
 */
@Service
public class UserServiceImpl implements com.lz.service.UserService {
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private AthleteDao athleteDao;
    
    @Autowired
    private EventDao eventDao;
    
    @Autowired
    private ProjectDao projectDao;
    
    @Autowired
    private RegistrationDao registrationDao;
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        User user;
        if(userLoginDTO.getUsername().contains("@")){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("Email",userLoginDTO.getUsername());
            queryWrapper.eq("Password",userLoginDTO.getPassword());
            user = userDao.selectOne(queryWrapper);
        }else {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("Username",userLoginDTO.getUsername());
            queryWrapper.eq("Password",userLoginDTO.getPassword());
            user = userDao.selectOne(queryWrapper);
        }
        
        return user;
    }

    /**
     * 列表
     *
     * @param listDto 列出 DTO
     *
     * @return {@code PageResult}
     */
    @Override
    public PageResult list(EventListDto listDto) {

        System.out.println("listDto:" + listDto);
        List<UserVO> list = userDao.selectAllAndState(listDto);

        int totalUserCount = userDao.getTotalUserCount(listDto);


        return new PageResult(totalUserCount,list);
    }

    @Override
    public void delete(String id) throws SQLException {
        QueryWrapper<Athlete> AthleteById = new QueryWrapper<>();
        AthleteById.eq("UserID",id);
        Athlete athlete = athleteDao.selectOne(AthleteById);
        if(athlete != null ){
            throw new SQLException("删除失败，请删除相关信息");
        }

        int delete = userDao.deleteById(id);
        System.out.println("delete:" + delete);
        if (delete == 0) {
            throw new SQLException("删除失败，请刷新重试");
        }
        

        
        
    }

    /**
     * 检查播放器
     *
     * @param id 编号
     */
    @Override
    public void examinePlayer(String id) {
        User user = new User();
        user.setUserId(Long.valueOf(id));
        user.setUserType("运动员");
        userDao.updateById(user);

        Athlete athlete =
                Athlete.builder().agreeTime(new Date()).AthleteState("成功").build();

        QueryWrapper<Athlete> whereWrapper = new QueryWrapper<>();
        whereWrapper.eq("UserId", Long.valueOf(id));
        
        athleteDao.update(athlete,whereWrapper);
    }

    /**
     * 按月获取学生数量
     *
     * @param month 子
     *
     * @return {@code UserData}
     */
    @Override
    public UserData getUserNumsByMonth(String month) {
        UserData userData = new UserData();
        userData.setDate(month);
        userData.setAddUser(userDao.getUserNumsByMonth(month));
        userData.setAddAthlete(athleteDao.getAthleteNumsByMonth(month));
        return userData;
    }

    /**
     * 获取用户类型
     *
     * @return {@code List<UserType>}
     */
    @Override
    public List<UserType> getUserTypes() {

        List<UserType> userTypes = new ArrayList<>();

        UserType userType1 = new UserType();
        Integer userNums = userDao.getUserTotal();
        userType1.setType("学生");
        userType1.setNums(userNums);
        userTypes.add(userType1);

        UserType userType2 = new UserType();
        Integer registrationNums= registrationDao.getRegistrationPlayerTotal();
        userType2.setType("已参加项目的运动员");
        userType2.setNums(registrationNums);
        userTypes.add(userType2);

        UserType userType3 = new UserType();
        Integer athleteNums= athleteDao.getAthleteTotal();
        userType3.setType("未参加项目的运动员");
        userType3.setNums(athleteNums-registrationNums);
        userTypes.add(userType3);
        return userTypes;
    }

    /**
     * 获取 NUMS
     *
     * @return {@code int[]}
     */
    @Override
    public int[] getNums() {
        int[] nums = new int[6];
        nums[0] = athleteDao.getAthleteTotal();
        nums[1] = eventDao.getEventTotal();
        nums[2] = projectDao.getProjectTotal();
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 获取当前年份和月份
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        nums[3] = athleteDao.getAthleteNumByMonth(year,month);
        nums[4] = eventDao.getEventNumsByMonth(year,month);
        nums[5] = projectDao.getProjectNumsByMonth(year,month);
        return nums;
    }

    /**
     * 查找用户信息
     *
     * @return {@code List<UserType>}
     */
    @Override
    public User selectUserInfo() {
        Long currentId = BaseContext.getCurrentId();
        User user = userDao.selectById(currentId);
        return user;
    }


}