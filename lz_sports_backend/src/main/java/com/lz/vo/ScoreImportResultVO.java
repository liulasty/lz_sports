package com.lz.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreImportResultVO {
    private Integer successCount = 0;
    private Integer failCount = 0;
    private List<ScoreImportFailureVO> failures = new ArrayList<>();
}
