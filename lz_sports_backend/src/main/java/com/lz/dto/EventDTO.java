package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Event Creation/Update DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO implements Serializable {
    private String[] date1; // [startDate, endDate]
    private String name;
    private String fee;
    private String type; // Eligibility
    
    // Admin user IDs
    private List<Long> adminIds;

    // For compatibility with frontend sending image objects
    // In new backend, we might simplify this, but let's keep it close to old structure for now
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
                addImage[i] = imageUrls[i].getOssUrl();
            }
        }
        if (deleteImagesUrls != null) {
            deleteImage = new String[deleteImagesUrls.length];
            for (int i = 0; i < deleteImagesUrls.length; i++) {
                deleteImage[i] = deleteImagesUrls[i].getOssUrl();
            }
        }
    }
}
