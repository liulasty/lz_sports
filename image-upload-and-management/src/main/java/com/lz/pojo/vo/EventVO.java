package com.lz.pojo.vo;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/14/11:27
 * @Description:
 */

import com.lz.pojo.dto.ImgDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lz
 */
@Data
@Builder
public class EventVO implements Serializable {
    private String date;
    private String end;
    private String name;
    private String fee;
    private String type;
    private List<String> imageUrls;
}