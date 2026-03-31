package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Project DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO implements Serializable {
    private String name;
    private Long event; // Event ID
    private String limitation;
    private String category;
    @Schema(description = "限制部门ID列表", example = "[1,2]")
    private List<Long> limitDeptIds;
    private Integer maxAttendance;
    private Integer attendance;
    private String[] date;
    private String startTime;
    private String endTime;
    
    // Image handling
    private ImgDTO[] imageUrls;
    private ImgDTO[] deleteImagesUrls;
    private String[] addImage;
    private String[] deleteImage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImgDTO implements Serializable {
        private String name;
        private String ossUrl;
    }

    public void mapOssUrlToAddImage() {
        if (imageUrls != null) {
            addImage = new String[imageUrls.length];
            for (int i = 0; i < imageUrls.length; i++) {
                String url = imageUrls[i].getOssUrl();
                addImage[i] = url;
            }
        }
    }

    public String getStartTime() {
        if (startTime != null && !startTime.isEmpty()) {
            return startTime;
        }
        if (date != null && date.length > 0) {
            return date[0];
        }
        return null;
    }

    public String getEndTime() {
        if (endTime != null && !endTime.isEmpty()) {
            return endTime;
        }
        if (date != null && date.length > 1) {
            return date[1];
        }
        return null;
    }
}
