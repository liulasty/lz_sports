package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Score;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.vo.ScoreVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
    IPage<ScoreVO> selectScorePage(Page<ScoreVO> page, @Param("eventName") String eventName);
}
