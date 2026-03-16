package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.common.result.PageResult;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.entity.Score;
import com.lz.vo.ScoreImportResultVO;
import com.lz.vo.ScoreVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface ScoreService extends IService<Score> {
    void upsertScore(ScoreUpsertDTO dto);

    void updateScore(Long scoreId, ScoreUpsertDTO dto);

    PageResult list(int currentPage, int pageSize, String eventName, Long eventId, Long itemId, Boolean onlyPublished);

    void publishScores(Long eventId);

    ScoreImportResultVO importScores(MultipartFile file, Long eventId);

    void downloadTemplate(Long eventId, HttpServletResponse response);

    void exportScores(Long eventId, HttpServletResponse response);

    void exportRegistrations(Long eventId, HttpServletResponse response);

    List<ScoreVO> myScores(Long eventId);

    Map<String, List<ScoreVO>> publicRanking(Long eventId);
}
