package com.lz.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/02/10:57
 * @Description:
 */

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.lz.Dao.SportsImgDao;
import com.lz.context.BaseContext;
import com.lz.pojo.entity.SportsImg;
import com.lz.pojo.result.Result;
import com.lz.service.ImgService;
import com.lz.service.SportsImgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author lz
 */
@RestController
@RequestMapping("sports/img")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ImgController {
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
    
    @Autowired
    private SportsImgService sportsImgService;

    /**
     * 上传图片
     *
     * @param file 文件
     *
     * @return {@code ResponseEntity<String>}
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        try {
            String folderName = "photos/";
            // 生成唯一的文件名
            String fileName = folderName + UUID.randomUUID() + ".png";

            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 上传文件到阿里云Bucket
            ossClient.putObject(bucketName, fileName, file.getInputStream());

            // 关闭OSSClient
            ossClient.shutdown();

            // 返回上传成功的消息
            headers.add("Custom-Header", "Image_uploaded_successfully");
            headers.add("file-Name", fileName);

            // 构建 ResponseEntity，并设置响应头和响应状态码
            ResponseEntity<String> responseEntity = ResponseEntity
                    .status(HttpStatus.OK)  // 设置响应状态码
                    .headers(headers)  // 设置响应头
                    .body(fileName);  // 设置响应体

            return responseEntity;
        } catch (IOException e) {
            e.printStackTrace();
            headers.add("Custom-Header", "Failed_to_upload_image");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Failed_to_upload_image");
        }
    }

    /**
     * 删除图像
     *
     * @param deleteImagesUrl 删除图像 URL
     *
     * @return {@code ResponseEntity<String>}
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImages(String deleteImagesUrl) {
        HttpHeaders headers = new HttpHeaders();

        String deleteImg =
                deleteImagesUrl.substring(deleteImagesUrl.lastIndexOf('/') + 1);
        try {
            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, deleteImg);
            System.out.println("文件删除成功");
            System.out.println("deleteImagesUrl = " + deleteImg);
            // 构建 ResponseEntity，并设置响应头和响应状态码
            ResponseEntity<String> responseEntity = ResponseEntity
                    // 设置响应状态码
                    .status(HttpStatus.OK)
                    // 设置响应头
                    .headers(headers)
                    // 设置响应体
                    .body(deleteImg);

            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Failed_to_delete_image");

        }
    }

    /**
     * 图像列表
     *
     * @param imageList 图像列表
     *
     * @return {@code ResponseEntity<String>}
     */
    @PostMapping("/imageList")
    public ResponseEntity<String> imageList(@RequestBody String[] imageList) {

        try {
            System.out.println("imageList = " + Arrays.toString(imageList));
            return ResponseEntity.status(HttpStatus.OK).body(
                    "接受成功");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "未接受到集合");
        }
    }

    /**
     * 获取事件 img
     *
     * @return {@code Result}
     */
    @GetMapping("/getEventImg/{eventId}")
    public Result getEventImg(@PathVariable Long eventId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("ImgType", "活动");
        map.put("typeId", eventId);
        try {
            List<SportsImg> sportsImgs = sportsImgService.selectByMap(map);

            return Result.success(sportsImgs, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.error("系统错误");

    }

    /**
     * 获取所有 IMG 名称
     *
     * @return {@code String}
     */
    @GetMapping("/getALLImg")
    public String getAllImgName() {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 构造ListObjectsRequest请求
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

        ArrayList<String> strings = new ArrayList<>();
        // 列举Bucket中所有文件
        ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
            strings.add(objectSummary.getKey());
        }

        // 关闭OSSClient
        ossClient.shutdown();

        return strings.toString();
    }

    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String fileName;
        try {
            String folderName = "avatar/";
            // 生成唯一的文件名
            fileName =
                    folderName + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 上传文件到阿里云Bucket
            ossClient.putObject(bucketName, fileName, file.getInputStream());

            // 关闭OSSClient
            ossClient.shutdown();
            System.out.println("fileName:" + fileName);

            Long currentId = BaseContext.getCurrentId();
            SportsImg sportsImg = SportsImg.builder().imgType("avatar")
                    .typeId(currentId)
                    .imgSrc(fileName).build();
            System.out.println("sportsImg:" + sportsImg);
            
            sportsImgService.addSrc(sportsImg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }


        return Result.success(bucketName+"."+endpoint+"/"+fileName,"上传成功" );
    }

}