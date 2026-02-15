package com.lz.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/20/13:02
 * @Description:
 */

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.Dao.SportsImgDao;
import com.lz.pojo.entity.SportsImg;
import com.lz.service.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * img 服务实现
 *
 * @author lz
 * @date 2023/11/20
 */
@Service
public class ImgServiceImpl implements ImgService {
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    
    @Autowired
    private SportsImgDao sportsImgDao;

    /**
     * 上传图片
     *
     * @param file 文件
     *
     * @return {@code String}
     */
    @Override
    public String uploadImage(MultipartFile file) {
        
        try {
            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 上传文件到阿里云Bucket
            ossClient.putObject(bucketName, fileName, file.getInputStream());

            // 关闭OSSClient
            ossClient.shutdown();
            
            

            return "Image_uploaded_successfully";
        } catch (IOException e) {
            e.printStackTrace();
            
            return "Failed_to_upload_image";
        }
    }

    /**
     * 删除图像
     *
     * @param deleteImagesUrl 删除图像 URL
     *
     * @return {@code String}
     */
    @Override
    public String deleteImages(String deleteImagesUrl){
        
        try {
            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, deleteImagesUrl);
            System.out.println("文件删除成功");
            System.out.println("deleteImagesUrl = " + deleteImagesUrl);

            LambdaQueryWrapper<SportsImg> lqw = new LambdaQueryWrapper<SportsImg>();
            lqw.ge(null != deleteImagesUrl, SportsImg::getTypeId,
                   deleteImagesUrl);
            sportsImgDao.delete(lqw);
            
            return "文件删除成功";
        } catch (Exception e) {
            e.printStackTrace();

            return "Failed_to_delete_image";

        }
    }

    /**
     * 图像列表
     *
     * @param imageList 图像列表
     *
     * @return {@code String}
     */
    @Override
    public  String imageList(String[] imageList){

        try {
            System.out.println("imageList = " + imageList.toString());
            return "接受成功";

        } catch (Exception e) {
            e.printStackTrace();
            return "未接受到集合";
        }
    }

    @Override
    public String imageAdd(String[] imageList) {
        
        return "添加成功";
    }
}