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
    private String[] date1;
    private String name;
    private String fee;
    private String type;
    private Integer maxItemsPerAthlete;
    private String registrationStartTime;
    private String registrationEndTime;
    private String eventStartTime;
    private String eventEndTime;
    private List<Long> adminIds;
    private ImgDTO[] imageUrls; 
    private ImgDTO[] deleteImagesUrls;
    private String[] addImage;
    private String[] deleteImage;

    public String getName() {
        return name;
    }

    public String[] getDate1() {
        return date1;
    }

    public String getType() {
        return type;
    }

    public String getRegistrationStartTime() {
        if (registrationStartTime != null && !registrationStartTime.isEmpty()) {
            return registrationStartTime;
        }
        if (date1 != null && date1.length > 0) {
            return date1[0];
        }
        return null;
    }

    public String getRegistrationEndTime() {
        if (registrationEndTime != null && !registrationEndTime.isEmpty()) {
            return registrationEndTime;
        }
        if (date1 != null && date1.length > 1) {
            return date1[1];
        }
        return null;
    }

    public String getEventStartTime() {
        if (eventStartTime != null && !eventStartTime.isEmpty()) {
            return eventStartTime;
        }
        if (date1 != null && date1.length > 2) {
            return date1[2];
        }
        return null;
    }

    public String getEventEndTime() {
        if (eventEndTime != null && !eventEndTime.isEmpty()) {
            return eventEndTime;
        }
        if (date1 != null && date1.length > 3) {
            return date1[3];
        }
        return null;
    }

    public String[] getAddImage() {
        return addImage;
    }

    public List<Long> getAdminIds() {
        return adminIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImgDTO implements Serializable {
        private String name;
        private String ossUrl;

        public String getOssUrl() {
            return ossUrl;
        }

        public void setOssUrl(String ossUrl) {
            this.ossUrl = ossUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
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
