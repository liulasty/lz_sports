package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.dto.EventListDTO;
import com.lz.entity.User;
import com.lz.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Update("UPDATE sys_user SET unread_count = unread_count + 1 WHERE id = #{userId}")
    int incrementUnreadCount(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET unread_count = CASE WHEN unread_count > 0 THEN unread_count - 1 ELSE 0 END WHERE id = #{userId}")
    int decrementUnreadCount(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET unread_count = 0 WHERE id = #{userId}")
    int resetUnreadCount(@Param("userId") Long userId);
}
