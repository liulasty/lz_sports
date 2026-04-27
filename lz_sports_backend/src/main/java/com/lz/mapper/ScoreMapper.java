package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Score;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.vo.ScoreVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
    IPage<ScoreVO> selectScorePage(Page<ScoreVO> page,
                                   @Param("eventId") Long eventId,
                                   @Param("itemId") Long itemId,
                                   @Param("eventName") String eventName,
                                   @Param("onlyPublished") Boolean onlyPublished);

    int upsertByRegistration(@Param("registrationId") Long registrationId,
                             @Param("eventId") Long eventId,
                             @Param("itemId") Long itemId,
                             @Param("userId") Long userId,
                             @Param("scoreValue") String scoreValue,
                             @Param("scoreRank") Integer scoreRank,
                             @Param("remark") String remark);

    List<ScoreVO> selectPublicByEvent(@Param("eventId") Long eventId);

    List<ScoreVO> selectScoreVOByIds(@Param("ids") List<Long> ids);

    @Select("""
            SELECT event_id as eventId, COUNT(DISTINCT item_id) as publishedCount
            FROM result
            WHERE is_published = 1
            GROUP BY event_id
            """)
    List<java.util.Map<String, Object>> countPublishedProjectsGroupByEvent();
}
