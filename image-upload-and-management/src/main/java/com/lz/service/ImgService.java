package com.lz.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * IMG服务
 * Created with IntelliJ IDEA.
 *
 * @author lz
 * @Author: lz
 * @Date: 2023/11/20/13:02
 * @Description:
 * @date 2023/11/20
 */
public interface ImgService {
    /**
     * 上传图片
     *
     * @param file 文件
     *
     * @return {@code String}
     */
    String uploadImage(MultipartFile file);

    /**
     * 删除图像
     *
     * @param deleteImagesUrl 删除图像 URL
     *
     * @return {@code String}
     */
    String deleteImages(String deleteImagesUrl);

    /**
     * 图像列表
     *
     * @param imageList 图像列表
     *
     * @return {@code String}
     */
    String imageList(String[] imageList);

    /**
     * 图片添加
     *
     * @param imageList 图像列表
     *
     * @return {@code String}
     */
    String imageAdd(String[] imageList);
}