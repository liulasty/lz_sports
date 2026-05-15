package com.lz.eligibility.engine;

import lombok.Value;

/**
 * 资格校验结果。
 * <p>
 * passed=true 时 reason 为空；passed=false 时 reason 描述失败原因（维度+组描述）。
 */
@Value
public class EligibilityResult {
    boolean passed;
    /** 失败原因（人类可读），passed=true 时为空 */
    String reason;

    public static EligibilityResult ok() {
        return new EligibilityResult(true, null);
    }

    public static EligibilityResult fail(String reason) {
        return new EligibilityResult(false, reason);
    }
}
