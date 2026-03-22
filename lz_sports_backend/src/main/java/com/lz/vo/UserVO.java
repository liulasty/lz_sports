package com.lz.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * User VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "用户信息返回对象")
public class UserVO {
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Builder.Default
    @Schema(description = "用户名", example = "admin")
    private String username = "";
    
    @Builder.Default
    @Schema(description = "真实姓名", example = "张三")
    private String name = "";
    
    @Builder.Default
    @Schema(description = "性别", example = "UNKNOWN")
    private String gender = "UNKNOWN";
    
    @Builder.Default
    @Schema(description = "学号", example = "20230001")
    private String studentId = "";
    
    @Builder.Default
    @Schema(description = "年级ID", example = "1")
    private Long gradeId = 0L;
    
    @Builder.Default
    @Schema(description = "邮箱", example = "test@example.com")
    private String email = "";
    
    @Builder.Default
    @Schema(description = "学校ID", example = "1")
    private Long schoolId = 0L;
    
    @Builder.Default
    @Schema(description = "头像URL", example = "https://lz-sports.oss-cn-beijing.aliyuncs.com/default-avatar.png")
    private String avatar = "";

    @Schema(description = "注册时间", example = "2023-10-01T12:00:00.000Z")
    private Date registerTime;
    
    @Schema(description = "状态", example = "ACTIVE")
    private String state;
    
    @Schema(description = "角色", example = "ATHLETE")
    private String type;
    
    @Schema(description = "申请状态", example = "PENDING")
    private String applyState;
    
    @Schema(description = "申请时间", example = "2023-10-01T12:00:00.000Z")
    private String applyTime;
}
