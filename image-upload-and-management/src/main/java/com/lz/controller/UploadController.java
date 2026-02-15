package com.lz.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author lz
 * @date 2023/10/06
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class UploadController {

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        try {
            // 生成唯一的文件名
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

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

            return ResponseEntity
                    // 设置响应状态码
                    .status(HttpStatus.OK)
                    // 设置响应头
                    .headers(headers)
                    // 设置响应体
                    .body(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            headers.add("Custom-Header", "Failed_to_upload_image");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Failed_to_upload_image");
        }
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImages(String deleteImagesUrl){
        HttpHeaders headers = new HttpHeaders();
        try {
            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, deleteImagesUrl);
            System.out.println("文件删除成功");
            System.out.println("deleteImagesUrl = " + deleteImagesUrl);
            // 构建 ResponseEntity，并设置响应头和响应状态码

            return ResponseEntity
                    // 设置响应状态码
                    .status(HttpStatus.OK)
                    // 设置响应头
                    .headers(headers)
                    // 设置响应体
                    .body(deleteImagesUrl);
        } catch (Exception e) {
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Failed_to_delete_image");
            
        }
    }
    
    @PostMapping("/imageList")
    public  ResponseEntity<String> imageList(@RequestBody String[] imageList){
        
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
}