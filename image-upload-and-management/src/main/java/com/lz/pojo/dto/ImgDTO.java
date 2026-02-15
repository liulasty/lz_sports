package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/29/12:18
 * @Description:
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @author lz
 */
@Data
public class ImgDTO implements Serializable {
    private String url;
    private String type;
    private String ossUrl;

    
}