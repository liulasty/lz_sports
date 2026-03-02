package com.lz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.exception.BusinessException;
import com.lz.entity.Score;
import com.lz.listener.ScoreImportListener;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.service.ScoreService;
import com.lz.vo.ScoreImportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.common.result.PageResult;
import com.lz.vo.ScoreVO;
import com.lz.mapper.ScoreMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private final RegistrationMapper registrationMapper;

    @Override
    public PageResult list(int currentPage, int pageSize, String eventName) {
        Page<ScoreVO> page = new Page<>(currentPage, pageSize);
        IPage<ScoreVO> result = baseMapper.selectScorePage(page, eventName);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importScores(MultipartFile file, Long eventId) {
        try {
            EasyExcel.read(file.getInputStream(), ScoreImportVO.class, 
                new ScoreImportListener(this, registrationMapper, eventId)).sheet().doRead();
        } catch (IOException e) {
            log.error("Excel import failed", e);
            throw new BusinessException("Excel导入失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishScores(Long eventId) {
        LambdaUpdateWrapper<Score> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Score::getEventId, eventId);
        updateWrapper.set(Score::getIsPublished, true);
        update(updateWrapper);
    }
}
