package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.dto.EventListDTO;
import com.lz.entity.User;
import com.lz.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * User Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByEmail(String email);

    List<UserVO> selectAllAndState(@Param("listDto") EventListDTO listDto);

    int getTotalUserCount(@Param("listDto") EventListDTO listDto);

    int getUserNumsByMonth(String month);

    @Select("select COUNT(*) from user where UserType='学生'")
    Integer getUserTotal();
}
