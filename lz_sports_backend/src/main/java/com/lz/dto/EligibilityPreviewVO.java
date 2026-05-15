package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资格预览/批量检查返回值。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityPreviewVO {
    /** 项目 ID */
    private Long itemId;
    /** 是否通过 */
    private Boolean passed;
    /** 失败原因 */
    private String reason;
}
