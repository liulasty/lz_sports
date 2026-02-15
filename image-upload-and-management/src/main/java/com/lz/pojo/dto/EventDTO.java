package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/29/11:53
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author lz
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