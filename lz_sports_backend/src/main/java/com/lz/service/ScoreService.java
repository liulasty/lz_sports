package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.entity.Score;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.lz.common.result.PageResult;

public interface ScoreService extends IService<Score> {
    /**
     * Import scores from Excel
     */
    void importScores(MultipartFile file, Long eventId);

    /**
     * Publish scores for an event
     */
    void publishScores(Long eventId);

    /**
     * List scores
     */
    PageResult list(int currentPage, int pageSize, String eventName);
}
