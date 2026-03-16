package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId} AND is_read = 0")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE id = #{id} AND user_id = #{userId}")
    int countOwned(@Param("id") Long id, @Param("userId") Long userId);
}
