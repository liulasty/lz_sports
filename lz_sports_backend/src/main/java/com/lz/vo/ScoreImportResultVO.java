package com.lz.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreImportResultVO {
    private String mode = "BEST_EFFORT";
    private Boolean allOrNothing = false;
    private Integer successCount = 0;
    private Integer failCount = 0;
    private List<ScoreImportProjectStatVO> projectStats = new ArrayList<>();
    private String failureDetailFileName;
    private String failureDetailCsvBase64;
    private List<ScoreImportFailureVO> failures = new ArrayList<>();
}
