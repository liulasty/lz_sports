package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
    private String grade;
    private Integer maxAttendance;
    private Integer attendance;
    private String[] date; // [start, end]
    
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
                // Extract filename or keep full URL depending on logic. 
                // Old code did substring, but new backend might just store URL.
                // Sticking to old logic: substring last part
                String url = imageUrls[i].getOssUrl();
                addImage[i] = url; // Store full URL for simplicity in new backend
            }
        }
        // Similar for delete
    }
}
