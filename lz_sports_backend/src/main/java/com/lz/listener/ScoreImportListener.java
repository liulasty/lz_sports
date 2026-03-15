package com.lz.listener;

import com.lz.vo.ScoreImportVO;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.lz.service.ScoreService;
import com.lz.mapper.RegistrationMapper;
import com.lz.entity.Registration;
import com.lz.entity.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreImportListener implements ReadListener<ScoreImportVO> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScoreImportListener.class);

    private final ScoreService scoreService;
    private final RegistrationMapper registrationMapper;
    private final Long eventId;
    
    private static final int BATCH_COUNT = 100;
    private List<Score> cachedDataList = new ArrayList<>(BATCH_COUNT);

    public ScoreImportListener(ScoreService scoreService, RegistrationMapper registrationMapper, Long eventId) {
        this.scoreService = scoreService;
        this.registrationMapper = registrationMapper;
        this.eventId = eventId;
    }

    @Override
    public void invoke(ScoreImportVO data, AnalysisContext context) {
        if (data.getRegistrationId() == null) {
            return;
        }
        
        // Check if registration exists
        Registration r = registrationMapper.selectById(data.getRegistrationId());
        if (r == null) {
            log.warn("Registration not found: {}", data.getRegistrationId());
            return;
        }

        if (!r.getEventId().equals(eventId)) {
             log.warn("Event ID mismatch for Registration ID {}: expected {}, got {}", data.getRegistrationId(), eventId, r.getEventId());
             return;
        }

        Score score = new Score();
        score.setRegistrationId(r.getId());
        score.setEventId(r.getEventId());
        score.setItemId(r.getItemId());
        score.setAthleteId(r.getAthleteId()); // Changed from setUserId to setAthleteId
        score.setScoreValue(data.getScoreValue());
        score.setScoreRank(data.getScoreRank());
        score.setIsPublished(false);

        cachedDataList.add(score);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = new ArrayList<>(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("All data parsed!");
    }

    private void saveData() {
        if (!cachedDataList.isEmpty()) {
            scoreService.saveBatch(cachedDataList);
            log.info("Saved {} scores.", cachedDataList.size());
        }
    }
}
