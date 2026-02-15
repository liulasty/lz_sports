package com.lz.pojo.vo;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/12:01
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author lz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    
    private Date registerTime;
    
    private String name;
    
    private String state;
    
    private String type;
    
    private String applyState;
    
    private String applyTime;
}