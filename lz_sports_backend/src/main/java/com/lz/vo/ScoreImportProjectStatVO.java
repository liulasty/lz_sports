package com.lz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreImportProjectStatVO {
    private String itemName;
    private Integer successCount;
    private Integer failCount;
}
