package com.lz.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO implements Serializable {
    private String name;
    private Long event;
    private String limitation;
    private String grade;
    private Integer maxAttendance;
    private Integer attendance;
    private String[] date;
    private ImgDTO[] imageUrls;
    private ImgDTO[] deleteImagesUrls;
    private String[] addImage;
    private String[] deleteImage;

    /**
     * Map OSS URL 添加图片
     */
    public void mapOssUrlToAddImage() {
        if (imageUrls != null) {
            addImage = new String[imageUrls.length];
            for (int i = 0; i < imageUrls.length; i++) {
                addImage[i] =
                        imageUrls[i].getOssUrl().substring(imageUrls[i].getOssUrl().lastIndexOf('/') + 1);
            }
        }
        if (deleteImagesUrls != null) {
            deleteImage = new String[deleteImagesUrls.length];
            for (int i = 0; i < deleteImagesUrls.length; i++) {
                deleteImage[i] =
                        deleteImagesUrls[i].getOssUrl().substring(deleteImagesUrls[i].getOssUrl().lastIndexOf('/') + 1);
            }
        }
    }

}