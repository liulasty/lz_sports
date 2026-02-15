package com.lz.pojo.error;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2024/03/08/16:21
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private long timestamp;
    private String code;

    // 构造方法、Getter和Setter方法省略...

    // 可以添加其他字段，如errorCode等
}