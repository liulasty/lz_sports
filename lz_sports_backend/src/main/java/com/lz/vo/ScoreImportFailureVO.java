package com.lz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreImportFailureVO {
    private Integer rowNumber;
    private String reason;
}
